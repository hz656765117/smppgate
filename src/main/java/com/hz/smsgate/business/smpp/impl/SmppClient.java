package com.hz.smsgate.business.smpp.impl;


import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.exception.SmppChannelConnectException;
import com.hz.smsgate.base.smpp.exception.SmppTimeoutException;
import com.hz.smsgate.base.utils.MailUtil;
import com.hz.smsgate.base.utils.SpringContextUtil;
import com.hz.smsgate.business.mybatis.mapper.BindRecordMapper;
import com.hz.smsgate.business.pojo.BindRecord;
import com.hz.smsgate.business.pojo.CustomParam;
import com.hz.smsgate.business.service.SmppService;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SmppClient extends Client {

    private SmppReconnectionDaemon reconnectionDaemon = SmppReconnectionDaemon.getInstance();

    private Logger logger = LoggerFactory.getLogger(SmppClient.class);

    private final ScheduledThreadPoolExecutor monitorExecutor;
    private final ThreadPoolExecutor executor;
    private DefaultSmppClient clientBootstrap;
    private DefaultSmppSessionHandler sessionHandler;

    private SmppSessionConfiguration config;

    private ScheduledExecutorService enquireLinkExecutor;
    private ScheduledFuture<?> enquireLinkTask;
    private Integer enquireLinkPeriod = 10000;
    private Integer enquireLinkTimeout = 10000;
    private boolean shutdown = false;

    private BindRecord bindRecord;
    private BindRecordMapper bindRecordMapper = SpringContextUtil.getBean(BindRecordMapper.class);

    private volatile Integer connectionFailedTimes = 0;

    public SmppClient() {
        this.enquireLinkExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                String name = config.getName();
                t.setName("EnquireLink-" + name);
                return t;
            }
        });
        // for monitoring thread use, it's preferable to create your own instance
        // of an executor with Executors.newCachedThreadPool() and cast it to ThreadPoolExecutor
        // this permits exposing thinks like executor.getActiveCount() via JMX possible
        // no point renaming the threads in a factory since underlying Netty
        // framework does not easily allow you to customize your thread names
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        // to enable automatic expiration of requests, a second scheduled executor
        // is required which is what a monitor task will be executed with - this
        // is probably a thread pool that can be shared with between all client bootstraps
        monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory() {

            private AtomicInteger sequence = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppClientSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });

        // a single instance of a client bootstrap can technically be shared
        // between any sessions that are created (a session can go to any different
        // number of SMSCs) - each session created under
        // a client bootstrap will use the executor and monitorExecutor set
        // in its constructor - just be *very* careful with the "expectedSessions"
        // value to make sure it matches the actual number of total concurrent
        // open sessions you plan on handling - the underlying netty library
        // used for NIO sockets essentially uses this value as the max number of
        // threads it will ever use, despite the "max pool size", etc. set on
        // the executor passed in here
        clientBootstrap = new DefaultSmppClient(executor, 1, monitorExecutor);
    }

    public void initialize(SmppSessionConfiguration config, DefaultSmppSessionHandler smppClientMessageService) {
        this.config = config;
        logger = LoggerFactory.getLogger(SmppClient.class.getCanonicalName() + config.getName());
        //
        // setup configuration for a client session
        //
        sessionHandler = smppClientMessageService;
    }

    protected synchronized void reconnect(Integer connectionFailedTimes) {


        if (shutdown) {
            logger.warn("skipping reconnect for client {} due to shutdown", this);
            return;
        }
        if (!getConnectionFailedTimes().equals(connectionFailedTimes)) {
            logger.info("skipping reconnect for client {} due to optimistic lock", this, connectionFailedTimes,
                    getConnectionFailedTimes());
            return;
        }
        ++this.connectionFailedTimes;

        try {
            logger.info("connecting {}", this);

            disconnect();
            bindRecord = new BindRecord();
            bindRecord.setSystemid(config.getSystemId());
            bindRecord.setIp(config.getHost());
            bindRecord.setPort(config.getPort() + "");
            bindRecord.setType(0);
            bindRecord.setTime(new Date());
            smppSession = clientBootstrap.bind(config, sessionHandler);
            bindRecord.setStatus(0);
            logger.info("-----连接资源(systemid:{},host:{} port:{} sendId:{})成功------", config.getSystemId(), config.getHost(), config.getPort(), config.getAddressRange().getAddress());

            sessionHandler.setSmppSession(smppSession);

            this.connectionFailedTimes = 0;

            runEnquireLinkTask();

        } catch (SmppChannelConnectException e) {
            bindRecord.setStatus(1);
            logger.error("Unable to connect: " + e.getMessage() + " " + LoggingUtil.toString(getConfiguration()));
            logger.debug("", e);
            scheduleReconnect();
        } catch (SmppTimeoutException e) {
            bindRecord.setStatus(1);
            logger.error("Unable to connect: " + e.getMessage() + " " + LoggingUtil.toString(getConfiguration()));
            logger.debug("", e);
            scheduleReconnect();
        } catch (Exception e) {
            bindRecord.setStatus(1);
            logger.error("Unable to connect: " + LoggingUtil.toString(getConfiguration()), e);
            scheduleReconnect();
        }

        bindRecordMapper.insert(bindRecord);

        boolean flag = SpringContextUtil.getBean(SmppService.class).needBindRecord(config.getSystemId());
        //失败次数达标并且当前是绑定失败才告警休眠
        if (!flag && bindRecord.getStatus() == 1) {
            logger.error("-----近期连接次数太多,休眠5分钟，不去连接资源(systemid:{},host:{} port:{} sendId:{})------", config.getSystemId(), config.getHost(), config.getPort(), config.getAddressRange().getAddress());
            CustomParam customParam = SpringContextUtil.getBean(CustomParam.class);
            SpringContextUtil.getBean(MailUtil.class).sendSimpleMail(customParam.getMails(), "运营商状态异常", config.getSystemId() + "运营商连接异常，请管理员尽快关闭该运营商并反馈至运营商");
            try {
                Thread.sleep(300000);
            } catch (Exception e) {
                logger.error("休眠异常", e);
            }

            return;
        }


    }

    public void scheduleReconnect() {
        reconnectionDaemon.scheduleReconnect(this, connectionFailedTimes, createReconnectionTask());
    }


    private SmppReconnectionTask createReconnectionTask() {
        return new SmppReconnectionTask(this, connectionFailedTimes);
    }


    public synchronized void shutdown() {
        logger.info("Shutting down client {}", this);

        shutdown = true;
        disconnect();

        // this is required to not causing server to hang from non-daemon threads
        // this also makes sure all open Channels are closed to I *think*
        clientBootstrap.destroy();
        executor.shutdownNow();
        enquireLinkExecutor.shutdownNow();
        monitorExecutor.shutdownNow();
    }

    private void disconnect() {
        stopEnquireLinkTask();

        destroySession();
    }

    private void stopEnquireLinkTask() {
        if (enquireLinkTask != null) {
            this.enquireLinkTask.cancel(true);
        }
    }

    private void destroySession() {
        try {
            if (smppSession != null) {
                logger.debug("Cleaning up session... (final counters)");
                logCounters();

                smppSession.destroy();
                smppSession = null;
                // alternatively, could call close(), get outstanding requests from
                // the sendWindow (if we wanted to retry them later), then call shutdown()
            }
        } catch (Exception e) {
            logger.warn("Destroy session error", e);
        }
    }

    private void logCounters() {
        if (smppSession.hasCounters()) {
            logger.debug("tx-enquireLink: {}", smppSession.getCounters().getTxEnquireLink());
            logger.debug("tx-submitSM: {}", smppSession.getCounters().getTxSubmitSM());
            logger.debug("tx-deliverSM: {}", smppSession.getCounters().getTxDeliverSM());
            logger.debug("tx-dataSM: {}", smppSession.getCounters().getTxDataSM());
            logger.debug("rx-enquireLink: {}", smppSession.getCounters().getRxEnquireLink());
            logger.debug("rx-submitSM: {}", smppSession.getCounters().getRxSubmitSM());
            logger.debug("rx-deliverSM: {}", smppSession.getCounters().getRxDeliverSM());
            logger.debug("rx-dataSM: {}", smppSession.getCounters().getRxDataSM());
        }
    }


    private void runEnquireLinkTask() {
        enquireLinkTask = this.enquireLinkExecutor.scheduleWithFixedDelay(
                new SmppEnquireLinkTask(this, enquireLinkTimeout),
                enquireLinkPeriod, enquireLinkPeriod, TimeUnit.MILLISECONDS);
    }

    public Integer getConnectionFailedTimes() {
        return connectionFailedTimes;
    }

    @Override
    public SmppSessionConfiguration getConfiguration() {
        return config;
    }

    @Override
    public String toString() {
        return LoggingUtil.toString2(config);
    }

}
