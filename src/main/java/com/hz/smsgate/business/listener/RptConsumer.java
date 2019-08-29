package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.business.smpp.impl.DefaultSmppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;


/**
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
public class RptConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(RptConsumer.class);

	@Override
	public void run() {
		DeliverSm deliverSm;

		while (true) {

			BlockingQueue<Object> queue = null;
			try {
				queue = BDBStoredMapFactoryImpl.INS.getQueue("rptrvok", "rptrvok");
			} catch (Exception e) {
				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
			}

			try {
				;
//				if (ClientInit.list != null && ClientInit.list.size() > 0) {
//					DeliverSm deliverSm1 = ClientInit.list.get(0);
//					deliverSm1.setSequenceNumber(DefaultSmppSession.sequenceNumber.next());
//											DefaultSmppServer.smppSession.sendRequestPdu(deliverSm1, 3000, true);
//					ClientInit.list.remove(0);
//				}else {
//					Thread.sleep(1000);
//				}

				if (queue != null) {
					Object obj = queue.poll();
					if (obj != null) {
						deliverSm = (DeliverSm) obj;
						LOGGER.info("{}-读取到状态报告信息{}", Thread.currentThread().getName(), deliverSm.toString());
						DefaultSmppServer.smppSession.sendRequestPdu(deliverSm, 3000, true);
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
