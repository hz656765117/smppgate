package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.ClientInit;
import com.hz.smsgate.business.pojo.MsgVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * 长短信发送
 *
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class LongRealMtSendRedisConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongRealMtSendRedisConsumer.class);

	@Autowired
	public RedisUtil redisUtil;

	public static LongRealMtSendRedisConsumer longRealMtSendRedisConsumer;

	@PostConstruct
	public void init() {
		longRealMtSendRedisConsumer = this;
		longRealMtSendRedisConsumer.redisUtil = this.redisUtil;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}


		LOGGER.info("{}-长短信（redis）真实发送线程开始工作......", Thread.currentThread().getName());
		SubmitSm submitSm;
		SubmitSmResp submitResp;
		while (true) {

			try {
				if (longRealMtSendRedisConsumer.redisUtil != null) {
					Thread.sleep(1000);
					continue;
				}

				Object obj = longRealMtSendRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_OPT);
				if (obj == null) {
					//opt的短信发完后 处理通知的短信
					Object tzObj = longRealMtSendRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_TZ);
					if (tzObj != null) {
						longRealMtSendRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_OPT, tzObj);
						continue;
					}

					//通知的短信发完后 处理营销的短信
					Object yxObj = longRealMtSendRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_YX);
					if (yxObj != null) {
						longRealMtSendRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_OPT, yxObj);
						continue;
					}

					//三个通道都没消息 则休眠1s
					Thread.sleep(1000);
					continue;
				}


				submitSm = (SubmitSm) obj;
				//发送短信
				submitResp = realSend(submitSm);
				handleMsgId(submitResp, submitSm.getTempMsgId());


			} catch (Exception e) {
				LOGGER.error("{}-长短信分段下发异常", Thread.currentThread().getName(), e);
			}

		}

	}


	public SubmitSmResp realSend(SubmitSm submitSm) {
		SubmitSmResp submitResp = null;
		String sendId = "";
		String mbl = "";

		try {
			LOGGER.info("{}-读取到长短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());
			mbl = submitSm.getDestAddress().getAddress();
			sendId = submitSm.getSourceAddress().getAddress();

			//获取客户端session
			SmppSession session0 = PduUtils.getSmppSession(submitSm);

			submitSm.removeSequenceNumber();
			submitSm.calculateAndSetCommandLength();

			if (session0 == null) {
				longRealMtSendRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, submitSm.getTempMsgId());
				LOGGER.error("systemid({}),senderid({}),mbl（{}）获取客户端连接异常，丢弃该下行", submitSm.getSystemId(), sendId, mbl);
				return submitResp;
			}

			submitResp = session0.submit(submitSm, 10000);
		} catch (Exception e) {
			putSelfQueue(submitSm);
			LOGGER.error("{}-{}-{}- {} 处理短信下行异常", Thread.currentThread().getName(), submitSm.getSystemId(), sendId, mbl, e);
		}

		return submitResp;
	}


	/**
	 * 将真实msgid当key
	 *
	 * @param submitResp 上游返回下行响应
	 * @param msgId      自定义的msgid
	 */
	public void handleMsgId(SubmitSmResp submitResp, String msgId) {
		if (submitResp == null) {
			return;
		}
		String messageId = submitResp.getMessageId();
		//更新缓存中的value
		Object msgVo = longRealMtSendRedisConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, msgId);
		longRealMtSendRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, msgId);

		Object msgVo1 = longRealMtSendRedisConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, messageId);
		if (msgVo1 == null) {
			longRealMtSendRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, messageId, msgVo);
		} else {
			MsgVo msg = (MsgVo) msgVo;
			String msgId2 = msg.getMsgId();

			MsgVo msg1 = (MsgVo) msgVo1;
			String msgId1 = msg1.getMsgId();

			msg1.setMsgId(msgId1 + "|" + msgId2);
			longRealMtSendRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, messageId, msg1);
		}


	}


	/**
	 * 将发送失败的非opt短信放入到营销中，继续下一次发送
	 *
	 * @param submitSm 下行短信对象
	 */
	public void putSelfQueue(SubmitSm submitSm) {
		try {
			if (submitSm.getSourceAddress() == null) {
				LOGGER.error("{} 长短信 下行对象为空，将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName());
				return;
			}
			String senderId = submitSm.getSourceAddress().getAddress();
			SessionKey sessionKey = new SessionKey(submitSm.getSystemId(), senderId);
			if (!ClientInit.CHANNEL_OPT_LIST.contains(sessionKey)) {
				submitSm.removeSequenceNumber();
				submitSm.calculateAndSetCommandLength();
				longRealMtSendRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_YX, submitSm);
				LOGGER.info("{} 长短信 将发送失败的非opt短信放入到营销中", Thread.currentThread().getName(), submitSm.toString());
				Thread.sleep(500);
			} else {
				LOGGER.error("systemid({}),senderid({}) 为OPT短信，丢弃该下行{}", sessionKey.getSystemId(), sessionKey.getSenderId(), submitSm.toString());
			}
		} catch (Exception e) {
			LOGGER.error("{} 长短信 将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName(), e);
		}
	}


}
