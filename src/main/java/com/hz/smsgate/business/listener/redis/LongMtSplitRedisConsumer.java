package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;


/**
 * 将长短信拆分并放到真正的发送队列中
 *
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class LongMtSplitRedisConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongMtSplitRedisConsumer.class);


	@Autowired
	public RedisUtil redisUtil;

	public static LongMtSplitRedisConsumer longMtSplitRedisConsumer;

	@PostConstruct
	public void init() {
		longMtSplitRedisConsumer = this;
		longMtSplitRedisConsumer.redisUtil = this.redisUtil;
	}


	@Override
	public void run() {
		SubmitSm submitSm;


		while (true) {



//			BlockingQueue<Object> realSendQueue = null;
//			try {
//				realSendQueue = BDBStoredMapFactoryImpl.INS.getQueue("realLongSubmitSmSend", "realLongSubmitSmSend");
//			} catch (Exception e) {
//				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
//			}

			try {

				if (longMtSplitRedisConsumer.redisUtil != null) {
					Object obj = longMtSplitRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_LONG_SUBMIT_SM_SEND);
					if (obj != null) {
						submitSm = (SubmitSm) obj;
						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);

						LOGGER.info("{}-读取到长短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());
						splitSubmitSm1(submitSm);


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
	public static void splitSubmitSm1(SubmitSm submitSm) {

		try {

			byte[] shortMessage = submitSm.getShortMessage();
			int msgLen = shortMessage.length;
			LOGGER.info("{}-短信内容为{}-长度为{}", Thread.currentThread().getName(), new String(shortMessage), msgLen);
			//少于255个字符 不拆分短信
			if (msgLen < 255) {
				longMtSplitRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND, submitSm);
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
				longMtSplitRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND, submitSm);
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
