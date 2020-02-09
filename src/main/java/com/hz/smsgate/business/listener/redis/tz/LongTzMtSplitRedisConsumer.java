package com.hz.smsgate.business.listener.redis.tz;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * 将长短信拆分并放到真正的发送队列中(通知消息类型)
 *
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class LongTzMtSplitRedisConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongTzMtSplitRedisConsumer.class);


	@Autowired
	public RedisUtil redisUtil;

	public static LongTzMtSplitRedisConsumer longMtSplitRedisConsumer;

	@PostConstruct
	public void init() {
		longMtSplitRedisConsumer = this;
		longMtSplitRedisConsumer.redisUtil = this.redisUtil;
	}


	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}
		SubmitSm submitSm;
		Object obj;
		LOGGER.info("{}-处理将长短信拆分并放到真正的发送队列中(通知消息类型)线程（redis）开始工作......", Thread.currentThread().getName());


		while (true) {


			try {
				if (longMtSplitRedisConsumer.redisUtil != null) {
					obj = longMtSplitRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_LONG_SUBMIT_SM_SEND_TZ);
					if (obj != null) {
						submitSm = (SubmitSm) obj;
						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);
						splitSubmitSm(submitSm);
					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-处理将长短信拆分并放到真正的发送队列中(通知消息类型)异常", Thread.currentThread().getName(), e);
				try {
					Thread.sleep(10000);
				} catch (Exception E) {

				}
			}

		}

	}


	/**
	 * 长短信拆分
	 *
	 * @param submitSm
	 * @return
	 */
	public static void splitSubmitSm(SubmitSm submitSm) {

		try {

			byte[] shortMessage = submitSm.getShortMessage();
			int msgLen = shortMessage.length;
			LOGGER.info("{}-{} 短信内容为{}-长度为{}", Thread.currentThread().getName(), submitSm.getDestAddress().getAddress(), new String(shortMessage), msgLen);
			//少于255个字符 不拆分短信
			if (msgLen < 255) {
				longMtSplitRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_TZ, submitSm);
				return;
			}

			int msgNum = msgLen / 153;
			int lastMsgSize = msgLen % 153;
			int allMsgNum = lastMsgSize > 0 ? msgNum + 1 : msgNum;
			int index = 0;

			String[] tempMsgIds = submitSm.getTempMsgId().split("\\|");

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

				if (i < tempMsgIds.length) {
					String tempMsgId = tempMsgIds[i];
					ss.setTempMsgId(tempMsgId);
				}

				ss.calculateAndSetCommandLength();
				LOGGER.info("{}-长短信拆分{}-{}下行信息{}", Thread.currentThread().getName(), allMsgNum, (i + 1), ss.toString());
				longMtSplitRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_TZ, submitSm);
			}
		} catch (Exception e) {
			LOGGER.error("{}-长短信拆分异常", Thread.currentThread().getName(), e);
		}
	}


}
