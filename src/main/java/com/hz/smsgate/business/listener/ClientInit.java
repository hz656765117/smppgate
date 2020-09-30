package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.SystemGlobals;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppBindType;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.*;
import com.hz.smsgate.business.listener.redis.*;
import com.hz.smsgate.business.listener.redis.cmopt.LongCmOptMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.cmopt.LongCmOptMtSplitRedisConsumer;
import com.hz.smsgate.business.listener.redis.cmopt.LongCmOptRealMtSendRedisConsumer;
import com.hz.smsgate.business.listener.redis.opt.LongOptMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.opt.LongOptMtSplitRedisConsumer;
import com.hz.smsgate.business.listener.redis.tz.LongTzMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.tz.LongTzMtSplitRedisConsumer;
import com.hz.smsgate.business.listener.redis.yx.LongLongYxMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.yx.LongYxMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.yx.LongYxMtSplitRedisConsumer;
import com.hz.smsgate.business.mybatis.mapper.BindRecordMapper;
import com.hz.smsgate.business.pojo.BindRecord;
import com.hz.smsgate.business.pojo.CustomParam;
import com.hz.smsgate.business.pojo.OperatorVo;
import com.hz.smsgate.business.pojo.SmppUserVo;
import com.hz.smsgate.business.service.SmppService;
import com.hz.smsgate.business.smpp.handler.Client1SmppSessionHandler;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: huangzhuo
 * @Date: 2019/8/27 20:09
 * @Description:
 */

@Configuration
public class ClientInit implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ClientInit.class);

    @Autowired
    public RedisUtil redisUtil;

    public static ClientInit clientInit;


    @Autowired
    private SmppService smppService;


    public static Map<SessionKey, SmppSession> sessionMap = null;

    public static Map<SessionKey, SmppSessionConfiguration> configMap = null;

//    public static Map<String, DefaultSmppClient> clientBootstrapMap = null;
//
//    public static Map<String, DefaultSmppSessionHandler> sessionHandlerMap = null;


    public static Map<String, SessionKey> CHANNL_REL = new LinkedHashMap<>();


    public static List<SessionKey> CHANNEL_MK_LIST = new ArrayList<>();


    public static Map<SessionKey, WGParams> CHANNL_SP_REL = new LinkedHashMap<>();

    /**
     * opt通道
     */
    public static List<SessionKey> CHANNEL_OPT_LIST = new ArrayList<>();
    /**
     * 营销通道
     */
    public static List<SessionKey> CHANNEL_YX_LIST = new ArrayList<>();
    /**
     * 通知通道
     */
    public static List<SessionKey> CHANNEL_TZ_LIST = new ArrayList<>();


    public static List<SmppUserVo> HTTP_SMPP_USER = new LinkedList<>();

    public static Map<String, SmppSession> existSystemId1s = new LinkedHashMap<>();


    public void postConstruct() {

        clientInit = this;
        clientInit.redisUtil = this.redisUtil;

        //初始化配置文件
        initSystemGlobals();


        sessionMap = new LinkedHashMap<>();
        configMap = new LinkedHashMap<>();
//        clientBootstrapMap = new LinkedHashMap<>();
//        sessionHandlerMap = new LinkedHashMap<>();

        //初始化通道
        initChannels();
        //初始化优先级
        initYxj();
        //初始化mk集合
        initMkList();
        //初始化sp账号
        initSpList();
        //初始化http接入账号
        initHttpSmppUser();

        //初始化客户端配置
        initClientConfigs();


        //启动客户端
        if (configMap != null && configMap.size() > 0) {
            for (Map.Entry<SessionKey, SmppSessionConfiguration> entry : configMap.entrySet()) {
                String systemId = entry.getValue().getSystemId();
                String host = entry.getValue().getHost();
                String address = entry.getValue().getAddressRange().getAddress();
                String key = host + "|" + systemId;

                SessionKey sessionKey = new SessionKey();
                sessionKey.setSystemId(systemId);
                sessionKey.setSenderId(address);

                //同一个账号，不同通道 只建立一个客户端
                if (existSystemId1s.get(key) != null) {
                    sessionMap.put(sessionKey, existSystemId1s.get(key));
                    continue;
                }

                SmppSession client = createClient(entry.getValue());
                existSystemId1s.put(key, client);
            }
        }


        //启动相关线程
        initMutiThread();

    }

    public void initHttpSmppUser() {
        HTTP_SMPP_USER.clear();
        HTTP_SMPP_USER = smppService.getHttpAllSmppUser();
    }

    public void initClientConfigs() {
        try {

            //移除原有的配置
            LinkedHashSet keys = (LinkedHashSet) clientInit.redisUtil.hmGetAllKey("configMap");
            if (keys != null && keys.size() > 0) {
                Object[] objects = keys.toArray();
                clientInit.redisUtil.hmRemoves("configMap", objects);
            }
        } catch (Exception e) {
            logger.error("初始化通道配置异常", e);
        }

        ClientInit.configMap = getConfigs();

        //新增配置
        clientInit.redisUtil.hmPutAll("configMap", ClientInit.configMap);


    }


    public void initChannels() {
        Map<String, SessionKey> map = new LinkedHashMap<>();
        List<OperatorVo> allOperator = smppService.getAllOperator();
        for (OperatorVo operatorVo : allOperator) {
            SessionKey sessionKey = new SessionKey();
            sessionKey.setSenderId(operatorVo.getSenderid());
            sessionKey.setSystemId(operatorVo.getSystemid());
            map.put(operatorVo.getChannel(), sessionKey);
        }
        CHANNL_REL.clear();
        CHANNL_REL = map;
    }

    public void initSpList() {
        List<SmppUserVo> allSmppUser = smppService.getCmAllSmppUser();
        if (allSmppUser == null || allSmppUser.size() <= 0) {
            logger.error("未加载到sp账号");
            return;
        }

        Map<SessionKey, WGParams> configMap = new LinkedHashMap<>(allSmppUser.size());
        WGParams wgParams;
        SessionKey sessionKey;
        for (SmppUserVo smppUserVo : allSmppUser) {
            try {

                sessionKey = new SessionKey();
                sessionKey.setSenderId(smppUserVo.getSmppPwd());
                sessionKey.setSystemId(smppUserVo.getSmppUser());

                wgParams = new WGParams();
                wgParams.setSpid(smppUserVo.getSpUser());
                wgParams.setSppassword(smppUserVo.getSpPwd());

                configMap.put(sessionKey, wgParams);
            } catch (Exception e) {
                logger.error("sp账号解析异常！,过滤该配置", e);
                continue;
            }
        }
        CHANNL_SP_REL.clear();
        CHANNL_SP_REL = configMap;
    }


    public void initMkList() {
        List<OperatorVo> allOperator = smppService.getAllOperator();
        CHANNEL_MK_LIST.clear();
        for (OperatorVo operatorVo : allOperator) {
            if (operatorVo.getSystemid().toUpperCase().startsWith("HP")) {
                SessionKey sessionKey = new SessionKey(operatorVo.getSystemid(), operatorVo.getChannel());
                SessionKey sessionKey1 = ClientInit.CHANNL_REL.get(operatorVo.getChannel());
                CHANNEL_MK_LIST.add(sessionKey);
                CHANNEL_MK_LIST.add(sessionKey1);
            }
        }
    }

    public void initYxj() {
        List<OperatorVo> allOperator = smppService.getAllOperator();
        CHANNEL_OPT_LIST.clear();
        CHANNEL_TZ_LIST.clear();
        CHANNEL_YX_LIST.clear();
        for (OperatorVo operatorVo : allOperator) {
            SessionKey sessionKey = new SessionKey(operatorVo.getSystemid(), operatorVo.getChannel());
            if (operatorVo.getType() != null && 0 == operatorVo.getType()) {
                CHANNEL_OPT_LIST.add(sessionKey);
                CHANNEL_OPT_LIST.add(ClientInit.CHANNL_REL.get(operatorVo.getChannel()));
            } else if (operatorVo.getType() != null && 1 == operatorVo.getType()) {
                CHANNEL_TZ_LIST.add(sessionKey);
                CHANNEL_TZ_LIST.add(ClientInit.CHANNL_REL.get(operatorVo.getChannel()));
            } else {
                CHANNEL_YX_LIST.add(sessionKey);
                CHANNEL_YX_LIST.add(ClientInit.CHANNL_REL.get(operatorVo.getChannel()));
            }
        }


    }


    public Map<SessionKey, SmppSessionConfiguration> getConfigs() {
        List<OperatorVo> allOperator = smppService.getAllOperator();

        Map<SessionKey, SmppSessionConfiguration> configMap = new LinkedHashMap<>(allOperator.size());
        for (int i = 0; i < allOperator.size(); i++) {
            OperatorVo operatorVo = allOperator.get(i);
            if (StringUtils.isBlank(operatorVo.getIp())) {
                continue;
            }
            try {
                SmppSessionConfiguration config0 = new SmppSessionConfiguration();
                config0.setWindowSize(96);
                config0.setConnectTimeout(10000);
                config0.setRequestExpiryTimeout(30000);
                config0.setWindowMonitorInterval(15000);
                config0.setCountersEnabled(true);
                config0.getLoggingOptions().setLogBytes(true);
                config0.setType(SmppBindType.TRANSCEIVER);
                config0.setName("Tester.Session." + i);
                config0.setHost(operatorVo.getIp());
                config0.setPort(Integer.parseInt(operatorVo.getPort().trim()));
                config0.setSystemId(operatorVo.getSystemid().trim());
                config0.setPassword(operatorVo.getPassword().trim());

                String channel = operatorVo.getSenderid();
                config0.setAddressRange(new Address((byte) 0, (byte) 0, channel));

                SessionKey sessionKey = new SessionKey();
                sessionKey.setSenderId(config0.getAddressRange().getAddress());
                sessionKey.setSystemId(config0.getSystemId());
                configMap.put(sessionKey, config0);
            } catch (Exception e) {
                logger.error("客户端配置对象组装异常！,过滤该配置", e);
                continue;
            }
        }
        return configMap;
    }


    private static void initMutiThread() {

        ConfigLoadThread configLoadThread = new ConfigLoadThread();
        ThreadPoolHelper.executeTask(configLoadThread);

        ClientBindConsumer clientBindConsumer = new ClientBindConsumer();
        ThreadPoolHelper.executeTask(clientBindConsumer);


        MsgIdTimeOutRemoveThread msgIdTimeOutRemoveThread = new MsgIdTimeOutRemoveThread();
        ThreadPoolHelper.executeTask(msgIdTimeOutRemoveThread);


//		CleanLogThread cleanLogThread = new CleanLogThread();
//		ThreadPoolHelper.executeTask(cleanLogThread);

        RptRedisConsumer rptRedisConsumer = new RptRedisConsumer();

        LongOptMtMergeRedisConsumer longOptMtMergeRedisConsumer = new LongOptMtMergeRedisConsumer();
        LongOptMtSplitRedisConsumer longOptMtSplitRedisConsumer = new LongOptMtSplitRedisConsumer();
        LongCmOptRealMtSendRedisConsumer longCmOptRealMtSendRedisConsumer = new LongCmOptRealMtSendRedisConsumer();


        LongYxMtMergeRedisConsumer longYxMtMergeRedisConsumer = new LongYxMtMergeRedisConsumer();
        LongLongYxMtMergeRedisConsumer longLongYxMtMergeRedisConsumer = new LongLongYxMtMergeRedisConsumer();
        LongYxMtSplitRedisConsumer longYxMtSplitRedisConsumer = new LongYxMtSplitRedisConsumer();

        LongTzMtMergeRedisConsumer longTzMtMergeRedisConsumer = new LongTzMtMergeRedisConsumer();
        LongTzMtSplitRedisConsumer longTzMtSplitRedisConsumer = new LongTzMtSplitRedisConsumer();

        LongRealMtSendRedisConsumer longRealMtSendRedisConsumer = new LongRealMtSendRedisConsumer();

        //cm资源下行
        MtRedisCmConsumer mtRedisCmConsumer = new MtRedisCmConsumer();

        MtRedisConsumer mtRedisConsumer = new MtRedisConsumer();
        EnquireLinkConsumer enquireLinkConsumer = new EnquireLinkConsumer();
        SyncSubmitConsumer syncSubmitConsumer = new SyncSubmitConsumer();


        LongCmOptMtMergeRedisConsumer longCmOptMtMergeRedisConsumer = new LongCmOptMtMergeRedisConsumer();
        LongCmOptMtSplitRedisConsumer longCmOptMtSplitRedisConsumer = new LongCmOptMtSplitRedisConsumer();

        MtRecordThread mtRecordThread = new MtRecordThread();
        //记录详细下行数据线程
        ThreadPoolHelper.executeTask(mtRecordThread);

        //心跳线程
        ThreadPoolHelper.executeTask(enquireLinkConsumer);

        //同步下行信息到网关线程
        ThreadPoolHelper.executeTask(syncSubmitConsumer);


        RealChannelCallBackThread realChannelCallBackThread = new RealChannelCallBackThread();
        //真实channel回调线程
        ThreadPoolHelper.executeTask(realChannelCallBackThread);

        for (int i = 0; i <= 7; i++) {
            //CM 短信发送线程
            ThreadPoolHelper.executeTask(mtRedisCmConsumer);
        }

        //redis长短信cm合并   opt
        ThreadPoolHelper.executeTask(longCmOptMtMergeRedisConsumer);
        //redis长短信cm拆分   opt
        ThreadPoolHelper.executeTask(longCmOptMtSplitRedisConsumer);


        //redis长短信合并   opt
        ThreadPoolHelper.executeTask(longOptMtMergeRedisConsumer);
        //redis长短信拆分   opt
        ThreadPoolHelper.executeTask(longOptMtSplitRedisConsumer);


        for (int i = 0; i <= 10; i++) {
            //redis长短信发送
            ThreadPoolHelper.executeTask(longCmOptRealMtSendRedisConsumer);
        }


        //redis长短信合并   通知
        ThreadPoolHelper.executeTask(longTzMtMergeRedisConsumer);
        //redis长短信拆分  通知
        ThreadPoolHelper.executeTask(longTzMtSplitRedisConsumer);

        //redis长短信合并   营销
        ThreadPoolHelper.executeTask(longYxMtMergeRedisConsumer);
        //redis长长短信合并   营销
        ThreadPoolHelper.executeTask(longLongYxMtMergeRedisConsumer);
        //redis长短信拆分   营销
        ThreadPoolHelper.executeTask(longYxMtSplitRedisConsumer);

        for (int i = 0; i <= 15; i++) {
            //redis长短信发送
            ThreadPoolHelper.executeTask(longRealMtSendRedisConsumer);
        }

        for (int i = 0; i <= 12; i++) {
            //redis短短信下行线程
            ThreadPoolHelper.executeTask(mtRedisConsumer);
        }

        //redis状态报告处理线程
        for (int i = 0; i <= 3; i++) {
            ThreadPoolHelper.executeTask(rptRedisConsumer);
        }


        WebCmMtRedisOptConsumer webCmMtRedisOptConsumer = new WebCmMtRedisOptConsumer();
        for (int i = 0; i <= 8; i++) {
            //redis短短信下行线程
            ThreadPoolHelper.executeTask(webCmMtRedisOptConsumer);
        }


        PptRecordThread rptRecordThread = new PptRecordThread();
        ThreadPoolHelper.executeTask(rptRecordThread);


        MtRecordUpdateThread mtRecordUpdateThread = new MtRecordUpdateThread();
        ThreadPoolHelper.executeTask(mtRecordUpdateThread);

    }


    public static SmppSession createClient(SmppSessionConfiguration config) {
        if (config == null) {
            return null;
        }


        boolean flag = SpringContextUtil.getBean(SmppService.class).needBindRecord(config.getSystemId());
        if (!flag) {
            CustomParam customParam = SpringContextUtil.getBean(CustomParam.class);
            SpringContextUtil.getBean(MailUtil.class).sendSimpleMail(customParam.getMails(), "运营商状态异常", config.getSystemId() + "运营商连接异常，请管理员尽快关闭该运营商并反馈至运营商");
        }


        ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private AtomicInteger sequence = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("SmppClientSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });

        DefaultSmppClient clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), 1, monitorExecutor);
        DefaultSmppSessionHandler sessionHandler = new Client1SmppSessionHandler();

        SessionKey sessionKey = new SessionKey();
        sessionKey.setSenderId(config.getAddressRange().getAddress());
        sessionKey.setSystemId(config.getSystemId());

        SmppSession session0 = null;
        BindRecord bindRecord = null;
        try {
            bindRecord = new BindRecord();
            bindRecord.setSystemid(config.getSystemId());
            bindRecord.setIp(config.getHost());
            bindRecord.setPort(config.getPort() + "");
            bindRecord.setType(0);

            session0 = clientBootstrap.bind(config, sessionHandler);
            sessionHandler.setSmppSession(session0);
            logger.info("-----连接资源(systemid:{},host:{} port:{} sendId:{})成功------", config.getSystemId(), config.getHost(), config.getPort(), config.getAddressRange().getAddress());

            bindRecord.setStatus(0);

//            clientBootstrapMap.put(config.getSystemId(), clientBootstrap);
//            sessionHandlerMap.put(config.getSystemId(), sessionHandler);
            sessionMap.put(sessionKey, session0);
        } catch (Exception e) {
            bindRecord.setStatus(1);
            logger.error("连接资源(systemid:{},host:{} port:{} sendId:{})失败", config.getSystemId(), config.getHost(), config.getPort(), config.getAddressRange().getAddress(), e);
        }

        bindRecord.setTime(new Date());
        SpringContextUtil.getBean(BindRecordMapper.class).insert(bindRecord);

        return session0;
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


    @Override
    public void run(String... args) throws Exception {
        postConstruct();
    }
}
