package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.business.smpp.impl.DefaultSmppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;


/**
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
public class MtConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(MtConsumer.class);

	@Override
	public void run() {
		SubmitSm submitSm;


		while (true) {

			BlockingQueue<Object> queue = null;
			try {
				queue = BDBStoredMapFactoryImpl.INS.getQueue("submitSm", "submitSm");
			} catch (Exception e) {
				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
			}

			try {
				if (queue != null) {
					Object obj = queue.poll();
					if (obj != null) {
						submitSm = (SubmitSm) obj;

						SmppSession session0 = ClientInit.session0;
						if (session0 == null) {
							session0 = ClientInit.clientBootstrap.bind(ClientInit.config0, ClientInit.sessionHandler);
							ClientInit.session0 = session0;
						}
						LOGGER.info("{}-读取到状态报告信息{}", Thread.currentThread().getName(), submitSm.toString());
						SubmitSmResp submitResp = session0.submit(submitSm, 10000);
						BlockingQueue<Object> submitRespQueue = BDBStoredMapFactoryImpl.INS.getQueue("submitResp", "submitResp");
						submitRespQueue.put(submitResp);
//						DefaultSmppServer.smppSession.sendRequestPdu(submitResp, 3000, true);
						System.out.println("lllllllllll");
					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-处理短信状态报告转发异常", Thread.currentThread().getName(), e);
			}

		}


	}
}
