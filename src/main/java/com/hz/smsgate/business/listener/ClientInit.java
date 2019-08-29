package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pojo.SmppBindType;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.ThreadPoolHelper;
import com.hz.smsgate.business.smpp.handler.Client1SmppSessionHandler;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
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


	public static SmppSession session0 = null;

	public static DefaultSmppClient clientBootstrap = null;

	public static DefaultSmppSessionHandler sessionHandler = null;

	public static SmppSessionConfiguration config0 = null;

	@PostConstruct
	public void postConstruct() throws Exception {
		System.out.println("lll");

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
		ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory() {
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
		clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), 1, monitorExecutor);

		//
		// setup configuration for a client session
		//
		sessionHandler = new Client1SmppSessionHandler();

		config0 = new SmppSessionConfiguration();
		config0.setWindowSize(1);
		config0.setName("Tester.Session.0");
		config0.setType(SmppBindType.TRANSCEIVER);
//        config0.setHost("39.108.216.43");
//        config0.setPort(2776);
		config0.setHost("192.168.1.222");
		config0.setPort(8895);
		config0.setConnectTimeout(10000);
		config0.setSystemId("1234567890");
		config0.setPassword("password");
		config0.getLoggingOptions().setLogBytes(true);
		// to enable monitoring (request expiration)
		config0.setRequestExpiryTimeout(30000);
		config0.setWindowMonitorInterval(15000);
		config0.setCountersEnabled(true);


		try {
			// create session a session by having the bootstrap connect a
			// socket, send the bind request, and wait for a bind response
			session0 = clientBootstrap.bind(config0, sessionHandler);


		} catch (Exception e) {
			logger.error("", e);
		}




		RptConsumer rptConsumer = new RptConsumer();
		//多线程消费
		for (int i = 0; i < 1; i++) {
			ThreadPoolHelper.executeTask(rptConsumer);
		}

	}


}
