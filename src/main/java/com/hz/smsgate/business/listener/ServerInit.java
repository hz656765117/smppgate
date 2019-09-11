package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.constants.SystemGlobals;
import com.hz.smsgate.base.smpp.config.SmppServerConfiguration;
import com.hz.smsgate.base.utils.PropertiesLoader;
import com.hz.smsgate.business.smpp.handler.DefaultSmppServerHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: huangzhuo
 * @Date: 2019/8/16 15:26
 * @Description:
 */


@Configuration
public class ServerInit {
	private static final Logger logger = LoggerFactory.getLogger(ServerInit.class);

	@PostConstruct
	public void postConstruct() throws Exception {
		initSystemGlobals();

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		// to enable automatic expiration of requests, a second scheduled executor
		// is required which is what a monitor task will be executed with - this
		// is probably a thread pool that can be shared with between all client bootstraps
		ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory() {
			private AtomicInteger sequence = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
				return t;
			}
		});

		// create a server configuration
		SmppServerConfiguration configuration = new SmppServerConfiguration();
		configuration.setPort(StaticValue.SERVER_PORT);
		configuration.setMaxConnectionSize(10);
		configuration.setNonBlockingSocketsEnabled(true);
		configuration.setDefaultRequestExpiryTimeout(30000);
		configuration.setDefaultWindowMonitorInterval(15000);
		configuration.setDefaultWindowSize(50);
		configuration.setDefaultWindowWaitTimeout(configuration.getDefaultRequestExpiryTimeout());
		configuration.setDefaultSessionCountersEnabled(true);
		configuration.setJmxEnabled(true);

		// create a server, start it up
		DefaultSmppServer smppServer = new DefaultSmppServer(configuration, new DefaultSmppServerHandler(), executor, monitorExecutor);

		logger.info("Starting SMPP server...  port is {}", StaticValue.SERVER_PORT);
		smppServer.start();
		logger.info("SMPP server started");


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
