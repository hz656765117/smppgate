package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pdu.PduRequest;
import com.hz.smsgate.base.smpp.pdu.PduResponse;
import com.hz.smsgate.base.smpp.pojo.SmppBindType;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.business.smpp.impl.DefaultSmppClient;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
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
		sessionHandler = new ClientSmppSessionHandler();

		SmppSessionConfiguration config0 = new SmppSessionConfiguration();
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

		//
		// create session, enquire link, submit an sms, close session
		//


		try {
			// create session a session by having the bootstrap connect a
			// socket, send the bind request, and wait for a bind response
			session0 = clientBootstrap.bind(config0, sessionHandler);

//			System.out.println("Press any key to send enquireLink #1");
//			System.in.read();
//
//			// demo of a "synchronous" enquireLink call - send it and wait for a response
//			EnquireLinkResp enquireLinkResp1 = session0.enquireLink(new EnquireLink(), 10000);
//			logger.info("enquire_link_resp #1: commandStatus [" + enquireLinkResp1.getCommandStatus() + "=" + enquireLinkResp1.getResultMessage() + "]");
//			System.out.println("Press any key to send enquireLink #2");
//			System.in.read();
//
//			// demo of an "asynchronous" enquireLink call - send it, get a future,
//			// and then optionally choose to pick when we wait for it
//			WindowFuture<Integer, PduRequest, PduResponse> future0 = session0.sendRequestPdu(new EnquireLink(), 10000, true);
//			if (!future0.await()) {
//				logger.error("Failed to receive enquire_link_resp within specified time");
//			} else if (future0.isSuccess()) {
//				EnquireLinkResp enquireLinkResp2 = (EnquireLinkResp)future0.getResponse();
//				logger.info("enquire_link_resp #2: commandStatus [" + enquireLinkResp2.getCommandStatus() + "=" + enquireLinkResp2.getResultMessage() + "]");
//			} else {
//				logger.error("Failed to properly receive enquire_link_resp: " + future0.getCause());
//			}
//
//			System.out.println("Press any key to send submit #1");
//			System.in.read();
//
//			String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
//			byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM);
//
//			SubmitSm submit0 = new SubmitSm();
//
//			// add delivery receipt
//			//submit0.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
//
//			submit0.setSourceAddress(new Address((byte)0x03, (byte)0x00, "40404"));
//			submit0.setDestAddress(new Address((byte)0x01, (byte)0x01, "44555519205"));
//			submit0.setShortMessage(textBytes);
//
//			SubmitSmResp submitResp = session0.submit(submit0, 10000);
//
//
//			logger.info("sendWindow.size: {}", session0.getSendWindow().getSize());
//
//			System.out.println("Press any key to unbind and close sessions");
//			System.in.read();
//			session0.unbind(5000);
		} catch (Exception e) {
			logger.error("", e);
		}


	}


	/**
	 * Could either implement SmppSessionHandler or only override select methods
	 * by extending a DefaultSmppSessionHandler.
	 */
	public static class ClientSmppSessionHandler extends DefaultSmppSessionHandler {

		public ClientSmppSessionHandler() {
			super(logger);
		}

		@Override
		public void firePduRequestExpired(PduRequest pduRequest) {
			logger.warn("PDU request expired: {}", pduRequest);
		}

		@Override
		public PduResponse firePduRequestReceived(PduRequest pduRequest) {
			PduResponse response = pduRequest.createResponse();

			// do any logic here

			return response;
		}

	}
}
