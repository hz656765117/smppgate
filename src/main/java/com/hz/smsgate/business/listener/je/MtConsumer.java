package com.hz.smsgate.business.listener.je;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.business.listener.RptConsumer;
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
			String sendId = "";
			try {
				if (queue != null) {
					Object obj = queue.poll();
					if (obj != null) {
						submitSm = (SubmitSm) obj;
						sendId = submitSm.getSourceAddress().getAddress();

						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);

						//获取客户端session
						SmppSession session0 = PduUtils.getSmppSession(submitSm);

						LOGGER.info("{}-读取到状态报告信息{}", Thread.currentThread().getName(), submitSm.toString());
						SubmitSmResp submitResp = session0.submit(submitSm, 10000);





						String messageId = submitResp.getMessageId();


						//更新缓存中的value
						RptConsumer.CACHE_MAP.put(submitSm.getTempMsgId(), messageId);


						int msgLen = messageId.length();
						if (msgLen > 19) {
							messageId = messageId.substring(msgLen - 19, msgLen);
							submitResp.setMessageId(messageId);
							submitResp.setCommandLength(submitResp.getCommandLength() - (msgLen - 19));
						}

						BlockingQueue<Object> submitRespQueue = BDBStoredMapFactoryImpl.INS.getQueue("submitResp", "submitResp");
						submitRespQueue.put(submitResp);


					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-{}处理短信状态报告转发异常", Thread.currentThread().getName(), sendId, e);
			}

		}

	}


}
