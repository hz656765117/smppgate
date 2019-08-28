package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.smpp.config.SmppServerConfiguration;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.exception.SmppProcessingException;
import com.hz.smsgate.base.smpp.pdu.BaseBind;
import com.hz.smsgate.base.smpp.pdu.BaseBindResp;
import com.hz.smsgate.base.smpp.pojo.SmppServerHandler;
import com.hz.smsgate.base.smpp.pojo.SmppServerSession;
import com.hz.smsgate.business.smpp.handler.ServerSmppSessionHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppServer;
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
 * @Date: 2019/8/16 15:26
 * @Description:
 */


@Configuration
public class ServerInit {
	private static final Logger logger = LoggerFactory.getLogger(ServerInit.class);
	// 系统启动之后，如果需要初始化的某些东东，几种不同的方法：

	// 1
	@PostConstruct
	public void postConstruct() throws  Exception{
		System.out.println("system started, triggered by postConstruct.");


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
		configuration.setPort(2776);
		configuration.setMaxConnectionSize(10);
		configuration.setNonBlockingSocketsEnabled(true);
		configuration.setDefaultRequestExpiryTimeout(30000);
		configuration.setDefaultWindowMonitorInterval(15000);
		configuration.setDefaultWindowSize(5);
		configuration.setDefaultWindowWaitTimeout(configuration.getDefaultRequestExpiryTimeout());
		configuration.setDefaultSessionCountersEnabled(true);
		configuration.setJmxEnabled(true);

		// create a server, start it up
		DefaultSmppServer smppServer = new DefaultSmppServer(configuration, new DefaultSmppServerHandler(), executor, monitorExecutor);




		logger.info("Starting SMPP server...");
		smppServer.start();
		logger.info("SMPP server started");




		logger.info("Server counters: {}", smppServer.getCounters());










	}


	public static class DefaultSmppServerHandler implements SmppServerHandler {

		@Override
		public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, final BaseBind bindRequest) throws SmppProcessingException {
			// test name change of sessions
			// this name actually shows up as thread context....
			sessionConfiguration.setName("Application.SMPP." + sessionConfiguration.getSystemId());

			//throw new SmppProcessingException(SmppConstants.STATUS_BINDFAIL, null);
		}

		@Override
		public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) throws SmppProcessingException {
			logger.info("Session created: {}", session);
			// need to do something it now (flag we're ready)
			session.serverReady(new ServerSmppSessionHandler(session));
		}

		@Override
		public void sessionDestroyed(Long sessionId, SmppServerSession session) {
			logger.info("Session destroyed: {}", session);
			// print out final stats
			if (session.hasCounters()) {
				logger.info(" final session rx-submitSM: {}", session.getCounters().getRxSubmitSM());
			}

			// make sure it's really shutdown
			session.destroy();
		}

	}




//	// 5
//	@Bean
//	public CommandLineRunner initData(){
//		return (args) -> {
//			System.out.println("system started, triggered by CommandLineRunner.");
//			Stream.of(args).forEach(System.out::println);
//		};
//	}
//
//	// 4
//	@Bean
//	public ApplicationRunner initData2(){
//		return (args) -> {
//			System.out.println("system started, triggered by ApplicationRunner.");
//			Stream.of(args.getSourceArgs()).forEach(System.out::println);
//		};
//	}
//
//	// 3
//	@EventListener
//	public void onApplicationEvent(ContextRefreshedEvent event) {
//		System.out.println("system started, triggered by ContextRefreshedEvent.");
//	}
//
//	// 2
//	@Bean(initMethod = "init")
//	public InitMethodBean initMethodBean(){
//		return new InitMethodBean();
//	}
//
//	private static class InitMethodBean{
//		void init(){
//			System.out.println("system started, triggered by initMethod property.");
//		}
//	}



}
