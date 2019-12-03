package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.constants.SystemGlobals;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.FileUtils;
import com.hz.smsgate.base.utils.PropertiesLoader;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.base.utils.ThreadPoolHelper;
import com.hz.smsgate.business.listener.je.*;
import com.hz.smsgate.business.listener.redis.*;
import com.hz.smsgate.business.listener.redis.opt.LongOptMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.opt.LongOptMtSplitRedisConsumer;
import com.hz.smsgate.business.listener.redis.tz.LongTzMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.tz.LongTzMtSplitRedisConsumer;
import com.hz.smsgate.business.listener.redis.yx.LongYxMtMergeRedisConsumer;
import com.hz.smsgate.business.listener.redis.yx.LongYxMtSplitRedisConsumer;
import com.hz.smsgate.business.smpp.handler.Client1SmppSessionHandler;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ClientInit {
	private static final Logger logger = LoggerFactory.getLogger(ClientInit.class);

	@Autowired
	public RedisUtil redisUtil;

	public static ClientInit clientInit;


	public static Map<SessionKey, SmppSession> sessionMap = null;

	public static Map<SessionKey, SmppSessionConfiguration> configMap = null;

	public static Map<String, DefaultSmppClient> clientBootstrapMap = null;

	public static Map<String, DefaultSmppSessionHandler> sessionHandlerMap = null;


	@PostConstruct
	public void postConstruct() {

		clientInit = this;
		clientInit.redisUtil = this.redisUtil;

		//初始化配置文件
		initSystemGlobals();


		sessionMap = new LinkedHashMap<>();
		configMap = new LinkedHashMap<>();
		clientBootstrapMap = new LinkedHashMap<>();
		sessionHandlerMap = new LinkedHashMap<>();


		//初始化客户端配置
		initConfigs();

		Map<String, SmppSession> existSystemId1s = new LinkedHashMap<>();
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


	public static void initConfigs() {
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

		ClientInit.configMap = FileUtils.getConfigs(StaticValue.RESOURCE_HOME);
		//新增配置
		clientInit.redisUtil.hmPutAll("configMap", ClientInit.configMap);


	}


	private static void initMutiThread() {

//		try {
//			Thread.sleep(15000);
//		} catch (Exception e) {
//			logger.error("{}-线程初始化前休眠异常异常", Thread.currentThread().getName(), e);
//		}


		RptConsumer rptConsumer = new RptConsumer();
		MtConsumer mtConsumer = new MtConsumer();
		RptRedisConsumer rptRedisConsumer = new RptRedisConsumer();

		LongOptMtMergeRedisConsumer longOptMtMergeRedisConsumer = new LongOptMtMergeRedisConsumer();
		LongOptMtSplitRedisConsumer longOptMtSplitRedisConsumer = new LongOptMtSplitRedisConsumer();

		LongYxMtMergeRedisConsumer longYxMtMergeRedisConsumer = new LongYxMtMergeRedisConsumer();
		LongYxMtSplitRedisConsumer longYxMtSplitRedisConsumer = new LongYxMtSplitRedisConsumer();

		LongTzMtMergeRedisConsumer longTzMtMergeRedisConsumer = new LongTzMtMergeRedisConsumer();
		LongTzMtSplitRedisConsumer longTzMtSplitRedisConsumer = new LongTzMtSplitRedisConsumer();

		LongRealMtSendRedisConsumer longRealMtSendRedisConsumer = new LongRealMtSendRedisConsumer();

		//cm资源下行
		MtRedisCmConsumer mtRedisCmConsumer = new MtRedisCmConsumer();

		MtRedisConsumer mtRedisConsumer = new MtRedisConsumer();
		EnquireLinkConsumer enquireLinkConsumer = new EnquireLinkConsumer();
		SyncSubmitConsumer syncSubmitConsumer = new SyncSubmitConsumer();

		LongMtMergeConsumer longMtMergeConsumer = new LongMtMergeConsumer();
		LongMtSendConsumer longMtSendConsumer = new LongMtSendConsumer();


		LongRealMtSendConsumer longRealMtSendConsumer = new LongRealMtSendConsumer();


		//心跳线程
		ThreadPoolHelper.executeTask(enquireLinkConsumer);

		//同步下行信息到网关线程
		ThreadPoolHelper.executeTask(syncSubmitConsumer);


		for (int i = 0; i <= 1; i++) {
			//CM 短信发送线程
			ThreadPoolHelper.executeTask(mtRedisCmConsumer);
		}


		//0 je   1 redis
		if ("1".equals(StaticValue.TYPE)) {


			//redis长短信合并   opt
			ThreadPoolHelper.executeTask(longOptMtMergeRedisConsumer);
			//redis长短信拆分   opt
			ThreadPoolHelper.executeTask(longOptMtSplitRedisConsumer);

			//redis长短信合并   通知
			ThreadPoolHelper.executeTask(longTzMtMergeRedisConsumer);
			//redis长短信拆分  通知
			ThreadPoolHelper.executeTask(longTzMtSplitRedisConsumer);

			//redis长短信合并   营销
			ThreadPoolHelper.executeTask(longYxMtMergeRedisConsumer);
			//redis长短信拆分   营销
			ThreadPoolHelper.executeTask(longYxMtSplitRedisConsumer);


			for (int i = 0; i <= 5; i++) {
				//redis长短信发送
				ThreadPoolHelper.executeTask(longRealMtSendRedisConsumer);
			}

			for (int i = 0; i <= 3; i++) {
				//redis短短信下行线程
				ThreadPoolHelper.executeTask(mtRedisConsumer);
			}

			//redis状态报告处理线程
			for (int i = 0; i <= 3; i++) {
				ThreadPoolHelper.executeTask(rptRedisConsumer);
			}



		} else {
			//je长短信合并
			ThreadPoolHelper.executeTask(longMtMergeConsumer);
			//长短信拆分线程
			ThreadPoolHelper.executeTask(longMtSendConsumer);
			//长短信发送线程
			ThreadPoolHelper.executeTask(longRealMtSendConsumer);

			for (int i = 0; i <= 3; i++) {
				//je短短信下行线程
				ThreadPoolHelper.executeTask(mtConsumer);
			}

			//je状态报告处理线程
			ThreadPoolHelper.executeTask(rptConsumer);


		}


	}


	public static SmppSession createClient(SmppSessionConfiguration config) {
		if (config == null) {
			return null;
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
		try {
			session0 = clientBootstrap.bind(config, sessionHandler);
			sessionHandler.setSmppSession(session0);
			logger.info("-----连接资源(systemid:{},host:{} port:{} sendId:{})成功------", config.getSystemId(), config.getHost(), config.getPort(), config.getAddressRange().getAddress());

			clientBootstrapMap.put(config.getSystemId(), clientBootstrap);
			sessionHandlerMap.put(config.getSystemId(), sessionHandler);
			sessionMap.put(sessionKey, session0);
		} catch (Exception e) {
			logger.error("连接资源(systemid:{},host:{} port:{} sendId:{})失败", config.getSystemId(), config.getHost(), config.getPort(), config.getAddressRange().getAddress(), e);
		}
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


}
