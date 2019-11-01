package com.hz.smsgate.business.listener;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.smsgate.base.utils.PduUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


/**
 * 将长短信拆分并放到真正的发送队列中
 *
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
public class LongMtSendConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongMtSendConsumer.class);

	@Override
	public void run() {
		SubmitSm submitSm;


		while (true) {

			BlockingQueue<Object> queue = null;
			try {
				queue = BDBStoredMapFactoryImpl.INS.getQueue("longSubmitSmSend", "longSubmitSmSend");
			} catch (Exception e) {
				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
			}

			BlockingQueue<Object> realSendQueue = null;
			try {
				realSendQueue = BDBStoredMapFactoryImpl.INS.getQueue("realLongSubmitSmSend", "realLongSubmitSmSend");
			} catch (Exception e) {
				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
			}

			try {
				if (queue != null) {
					Object obj = queue.poll();
					if (obj != null) {
						submitSm = (SubmitSm) obj;
						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);

						LOGGER.info("{}-读取到长短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());
						splitSubmitSm1(submitSm, realSendQueue);


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
	 * 长短信拆分
	 *
	 * @param submitSm
	 * @return
	 */
	public static void splitSubmitSm1(SubmitSm submitSm, BlockingQueue<Object> realSendQueue) {

		try {

			byte[] shortMessage = submitSm.getShortMessage();
			int msgLen = shortMessage.length;
			LOGGER.info("{}-短信内容为{}-长度为{}", Thread.currentThread().getName(), new String(shortMessage) , msgLen);
			//少于255个字符 不拆分短信
			if (msgLen < 255) {
				realSendQueue.put(submitSm);
				return;
			}

			int msgNum = msgLen / 153;
			int lastMsgSize = msgLen % 153;
			int allMsgNum = lastMsgSize > 0 ? msgNum + 1 : msgNum;
			int index = 0;

			for (int i = 0; i < allMsgNum; i++) {
				SubmitSm ss = submitSm;
				byte[] shortMsg;

				if ((allMsgNum > msgNum) && (i == msgNum)) {
					shortMsg = new byte[lastMsgSize + 6];
				} else {
					shortMsg = new byte[159];
				}
				int realMsgLen = shortMsg.length - 6;

				shortMsg[0] = 05;
				shortMsg[1] = 00;
				shortMsg[2] = 03;
				shortMsg[3] = 39;
				shortMsg[4] = (byte) allMsgNum;
				shortMsg[5] = (byte) (i + 1);

				System.arraycopy(shortMessage, index, shortMsg, 6, realMsgLen);
				index += realMsgLen;
				ss.setShortMessage(shortMsg);
				ss.setEsmClass((byte) 00000100);
				ss.calculateAndSetCommandLength();
				LOGGER.info("{}-长短信拆分{}-{}下行信息{}", Thread.currentThread().getName(), allMsgNum, (i + 1), ss.toString());
				realSendQueue.put(ss);
			}
		} catch (Exception e) {
			LOGGER.error("{}-长短信拆分异常", Thread.currentThread().getName(), e);
		}
	}


	public static void splitSubmitSm(SubmitSm submitSm, BlockingQueue<Object> realSendQueue) {

		try {

			byte[] shortMessage = submitSm.getShortMessage();
			int msgLen = shortMessage.length;
			if (msgLen < 255) {
				realSendQueue.put(submitSm);
			}

			int msgNum = msgLen / 153;
			int lastMsgSize = msgLen % 153;
			int allMsgNum = lastMsgSize > 0 ? msgNum + 1 : msgNum;
			int index = 0;

			for (int i = 0; i < allMsgNum; i++) {
				SubmitSm ss = submitSm;
				byte[] shortMsg;

				if ((allMsgNum > msgNum) && (i == msgNum)) {
					shortMsg = new byte[lastMsgSize + 7];
				} else {
					shortMsg = new byte[160];
				}
				int realMsgLen = shortMsg.length - 7;

				shortMsg[0] = 0x06;
				shortMsg[1] = 0x08;
				shortMsg[2] = 0x04;
				shortMsg[3] = 0x00;
				shortMsg[4] = 0x39;
				shortMsg[5] = (byte) allMsgNum;
				shortMsg[6] = (byte) (i + 1);

				System.arraycopy(shortMessage, index, shortMsg, 7, realMsgLen);
				index += realMsgLen;
				ss.setShortMessage(shortMsg);
				ss.setEsmClass((byte) 00000100);
				ss.calculateAndSetCommandLength();
				LOGGER.info("{}-长短信拆分{}-{}下行信息{}", Thread.currentThread().getName(), allMsgNum, (i + 1), ss.toString());
				realSendQueue.put(ss);
			}
		} catch (Exception e) {
			LOGGER.error("{}-长短信拆分异常", Thread.currentThread().getName(), e);
		}
	}

}
