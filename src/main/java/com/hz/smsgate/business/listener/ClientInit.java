package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.constants.SystemGlobals;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppBindType;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.FileUtils;
import com.hz.smsgate.base.utils.PropertiesLoader;
import com.hz.smsgate.base.utils.ThreadPoolHelper;
import com.hz.smsgate.business.listener.redis.MtRedisConsumer;
import com.hz.smsgate.business.smpp.handler.Client1SmppSessionHandler;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
import com.hz.smsgate.business.smpp.handler.ServerSmppSessionHandler;
import com.hz.smsgate.business.smpp.handler.ServerSmppSessionRedisHandler;
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
		//初始化配置文件
		initSystemGlobals();


		sessionMap = new LinkedHashMap<>();
		configMap = new LinkedHashMap<>();
		clientBootstrapMap = new LinkedHashMap<>();
		sessionHandlerMap = new LinkedHashMap<>();


		//初始化客户端配置
		initConfigs();

		List<String> existSystemIds = new LinkedList<>();
		Map<String, SmppSession> existSystemId1s = new LinkedHashMap<>();
		//启动客户端
		if (configMap != null && configMap.size() > 0) {
			for (Map.Entry<String, SmppSessionConfiguration> entry : configMap.entrySet()) {
				String systemId = entry.getValue().getSystemId();
				String host = entry.getValue().getHost();
				String address = entry.getValue().getAddressRange().getAddress();
				String key = host + "|" + systemId;
				//同一个账号，不同通道 只建立一个客户端
				if (existSystemId1s.get(key) != null) {
					sessionMap.put(address, existSystemId1s.get(key));
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
		configMap = FileUtils.getConfigs(StaticValue.RESOURCE_HOME);
	}


	private static void initMutiThread() {
		RptConsumer rptConsumer = new RptConsumer();
		MtConsumer mtConsumer = new MtConsumer();
		MtRedisConsumer mtRedisConsumer = new MtRedisConsumer();

		LongMtConsumer longMtConsumer = new LongMtConsumer();
		LongMtSendConsumer longMtSendConsumer = new LongMtSendConsumer();
		EnquireLinkConsumer enquireLinkConsumer = new EnquireLinkConsumer();
		SyncSubmitConsumer syncSubmitConsumer = new SyncSubmitConsumer();

		ThreadPoolHelper.executeTask(longMtConsumer);
		ThreadPoolHelper.executeTask(enquireLinkConsumer);
		ThreadPoolHelper.executeTask(syncSubmitConsumer);



		//0 je   1 redis
		if ("1".equals(StaticValue.TYPE)) {
			ThreadPoolHelper.executeTask(mtRedisConsumer);
		} else {
			for (int i = 0; i <= 5; i++) {
				ThreadPoolHelper.executeTask(mtConsumer);
			}
			ThreadPoolHelper.executeTask(longMtSendConsumer);
		}


		//多线程消费
		for (int i = 0; i < 1; i++) {
			ThreadPoolHelper.executeTask(rptConsumer);
		}


	}


	public static SmppSession createClient(SmppSessionConfiguration config) {
		boolean flag = false;
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

		clientBootstrapMap.put(config.getAddressRange().getAddress(), clientBootstrap);
		sessionHandlerMap.put(config.getAddressRange().getAddress(), sessionHandler);
		SmppSession session0 = null;
		try {
			session0 = clientBootstrap.bind(config, sessionHandler);
			logger.info("-----连接资源(host:{} port:{} sendId:{})成功------", config.getHost(), config.getPort(), config.getAddressRange().getAddress());
			sessionMap.put(config.getAddressRange().getAddress(), session0);
			flag = true;
		} catch (Exception e) {
			logger.error("连接资源(host:{} port:{} sendId:{})失败", config.getHost(), config.getPort(), config.getAddressRange().getAddress(), e);
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
