package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.constants.SystemGlobals;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppBindType;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PropertiesLoader;
import com.hz.smsgate.base.utils.ThreadPoolHelper;
import com.hz.smsgate.business.smpp.handler.Client1SmppSessionHandler;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: huangzhuo
 * @Date: 2019/8/27 20:09
 * @Description:
 */

@Configuration
public class ClientInit {
	private static final Logger logger = LoggerFactory.getLogger(ClientInit.class);


	public static Map<String, SmppSession> sessionMap = null;

	public static Map<String, SmppSessionConfiguration> configMap = null;

	public static Map<String, DefaultSmppClient> clientBootstrapMap = null;

	public static Map<String, DefaultSmppSessionHandler> sessionHandlerMap = null;


	@PostConstruct
	public void postConstruct() throws Exception {

		initSystemGlobals();


		sessionMap = new LinkedHashMap<>();
		configMap = new LinkedHashMap<>();
		clientBootstrapMap = new LinkedHashMap<>();
		sessionHandlerMap = new LinkedHashMap<>();

		intConfigMap();

		SmppSession session0 = null;
//		SmppSessionConfiguration config0 = null;
		//
		// setup 3 things required for any session we plan on creating
		//

		// for monitoring thread use, it's preferable to create your own instance
		// of an executor with Executors.newCachedThreadPool() and cast it to ThreadPoolExecutor
		// this permits exposing thinks like executor.getActiveCount() via JMX possible
		// no point renaming the threads in a factory since underlying Netty
		// framework does not easily allow you to customize your thread names
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		// to enable automatic expiration of requests, a second scheduled executor
		// is required which is what a monitor task will be executed with - this
		// is probably a thread pool that can be shared with between all client bootstraps


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

		//
		// setup configuration for a client session
		//


//		config0 = new SmppSessionConfiguration();
//		config0.setWindowSize(1);
//		config0.setName("Tester.Session.0");
//		config0.setConnectTimeout(10000);
//		config0.setRequestExpiryTimeout(30000);
//		config0.setWindowMonitorInterval(15000);
//		config0.setCountersEnabled(true);
//		config0.getLoggingOptions().setLogBytes(true);
//		config0.setType(SmppBindType.TRANSCEIVER);
//		config0.setHost(StaticValue.CLIENT_HOST);
//		config0.setPort(StaticValue.CLIENT_PORT);
//		config0.setSystemId(StaticValue.CLIENT_SYSTEMID);
//		config0.setPassword(StaticValue.CLIENT_PASSWORD);
//		config0.setAddressRange(new Address((byte) 0, (byte) 0, "10086"));

		if (configMap != null && configMap.size() > 0) {

			for (Map.Entry<String, SmppSessionConfiguration> entry : configMap.entrySet()) {

				ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory() {
					private AtomicInteger sequence = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setName("SmppClientSessionWindowMonitorPool-" + sequence.getAndIncrement());
						return t;
					}
				});
				SmppSessionConfiguration config = entry.getValue();

				DefaultSmppClient clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), 1, monitorExecutor);
				DefaultSmppSessionHandler sessionHandler = new Client1SmppSessionHandler();

				clientBootstrapMap.put(config.getAddressRange().getAddress(),clientBootstrap);
				sessionHandlerMap.put(config.getAddressRange().getAddress(),sessionHandler);

				try {
					session0 = clientBootstrap.bind(config, sessionHandler);
					logger.info("-----连接资源(host:{} port:{} sendId:{})成功------", config.getHost(), config.getPort(), config.getAddressRange().getAddress());
					sessionMap.put(config.getAddressRange().getAddress(), session0);
				} catch (Exception e) {
					logger.error("连接资源失败", e);
				}
			}

		}


		RptConsumer rptConsumer = new RptConsumer();
		MtConsumer mtConsumer = new MtConsumer();
		LongMtConsumer longMtConsumer = new LongMtConsumer();
		LongMtSendConsumer longMtSendConsumer = new LongMtSendConsumer();
		EnquireLinkConsumer enquireLinkConsumer = new EnquireLinkConsumer();
		//多线程消费
		for (int i = 0; i < 1; i++) {
			ThreadPoolHelper.executeTask(rptConsumer);
			ThreadPoolHelper.executeTask(mtConsumer);
			ThreadPoolHelper.executeTask(longMtConsumer);
			ThreadPoolHelper.executeTask(longMtSendConsumer);
			ThreadPoolHelper.executeTask(enquireLinkConsumer);
		}

	}


	/**
	 * 初始化读取配置文件信息
	 */
	private void initSystemGlobals() {
		try {
			PropertiesLoader propertiesLoader = new PropertiesLoader();
			Properties properties = propertiesLoader.getProperties(SystemGlobals.SYSTEM_GLOBALS_NAME);
			SystemGlobals.setProperties(properties);
		} catch (Exception e) {
			logger.error("系统启动，初始化读取配置文件信息失败", e);
		}
	}

	public static void intConfigMap() {
		SmppSessionConfiguration config0 = new SmppSessionConfiguration();
		config0.setWindowSize(1);
		config0.setConnectTimeout(10000);
		config0.setRequestExpiryTimeout(30000);
		config0.setWindowMonitorInterval(15000);
		config0.setCountersEnabled(true);
		config0.getLoggingOptions().setLogBytes(true);
		config0.setType(SmppBindType.TRANSCEIVER);


		config0.setName("Tester.Session.0");
		config0.setHost(StaticValue.CLIENT_HOST);
		config0.setPort(StaticValue.CLIENT_PORT);
		config0.setSystemId(StaticValue.CLIENT_SYSTEMID);
		config0.setPassword(StaticValue.CLIENT_PASSWORD);
		config0.setAddressRange(new Address((byte) 0, (byte) 0, "10086"));


		configMap.put(config0.getAddressRange().getAddress(), config0);

		SmppSessionConfiguration config1 = new SmppSessionConfiguration();
		config1.setWindowSize(1);
		config1.setConnectTimeout(10000);
		config1.setRequestExpiryTimeout(30000);
		config1.setWindowMonitorInterval(15000);
		config1.setCountersEnabled(true);
		config1.getLoggingOptions().setLogBytes(true);
		config1.setType(SmppBindType.TRANSCEIVER);
		config1.setName("Tester.Session.1");
		config1.setHost("192.169.1.42");
		config1.setPort(8895);
		config1.setSystemId("123456");
		config1.setPassword("654321");
		config1.setAddressRange(new Address((byte) 0, (byte) 0, "10010"));

		configMap.put(config1.getAddressRange().getAddress(), config1);

	}


}
