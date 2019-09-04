package com.hz.smsgate.business.listener;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.PduUtil;
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

						submitSm = rewriteSubmitSm(submitSm);

						SmppSession session0 = ClientInit.session0;
						if (session0 == null) {
							session0 = ClientInit.clientBootstrap.bind(ClientInit.config0, ClientInit.sessionHandler);
							ClientInit.session0 = session0;
						}
						LOGGER.info("{}-读取到状态报告信息{}", Thread.currentThread().getName(), submitSm.toString());
						SubmitSmResp submitResp = session0.submit(submitSm, 10000);

						String messageId = submitResp.getMessageId();
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
				LOGGER.error("{}-处理短信状态报告转发异常", Thread.currentThread().getName(), e);
			}

		}

	}



	/**
	 * 重写下行对象，将通道更改为正确的 将短信内容编码   TODO
	 * @param sm
	 * @return
	 * @throws Exception
	 */
	public static SubmitSm rewriteSubmitSm(SubmitSm sm) throws Exception {
		byte[] shortMessage = sm.getShortMessage();
		String content = new String(shortMessage);
		LOGGER.info("短短信的内容为{}", content);
		byte[] textBytes = CharsetUtil.encode(content, CharsetUtil.CHARSET_GSM);
		LOGGER.info("短短信编码后的内容为{}", new String(textBytes));
		sm.setCommandLength(sm.getCommandLength() - sm.getShortMessage().length + textBytes.length);
		sm.setShortMessage(textBytes);


		Address sourceAddress = sm.getSourceAddress();
		int beforeLen = PduUtil.calculateByteSizeOfAddress(sourceAddress);
		sourceAddress.setAddress("CMK");
		int afterLen = PduUtil.calculateByteSizeOfAddress(sourceAddress);
		sm.setCommandLength(sm.getCommandLength() - beforeLen + afterLen);
		sm.setSourceAddress(sourceAddress);
		return sm;
	}
}
