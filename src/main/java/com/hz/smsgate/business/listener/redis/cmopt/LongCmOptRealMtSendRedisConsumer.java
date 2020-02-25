package com.hz.smsgate.business.listener.redis.cmopt;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.exception.SmppTimeoutException;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.Address;
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
 * Cm长短信发送
 *
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class LongCmOptRealMtSendRedisConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongCmOptRealMtSendRedisConsumer.class);

	@Autowired
	public RedisUtil redisUtil;

	public static LongCmOptRealMtSendRedisConsumer longOptRealMtSendRedisConsumer;

	@PostConstruct
	public void init() {
		longOptRealMtSendRedisConsumer = this;
		longOptRealMtSendRedisConsumer.redisUtil = this.redisUtil;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}


		LOGGER.info("{}-CM长短信（redis）OPT真实发送线程开始工作......", Thread.currentThread().getName());
		SubmitSm submitSm;
		SubmitSmResp submitResp;
		Object obj;
		while (true) {

			try {
				if (longOptRealMtSendRedisConsumer.redisUtil == null) {
					Thread.sleep(1000);
					continue;
				}

				obj = longOptRealMtSendRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_REL_CM_LONG_SUBMIT_SM_SEND_OPT);
				if (obj == null) {
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
				try {
					Thread.sleep(10000);
				} catch (Exception e1) {

				}
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


			if (session0 == null) {
//				longRealMtSendRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, submitSm.getTempMsgId());
				LOGGER.error("systemid({}),senderid({}),mbl（{}）获取客户端连接异常，丢弃该下行", submitSm.getSystemId(), sendId, mbl);
				return submitResp;
			}

			if ("infinotp".equals(session0.getConfiguration().getSystemId())) {
				Address destAddress = submitSm.getDestAddress();
				destAddress.setTon((byte) 1);
				destAddress.setNpi((byte) 1);
				submitSm.setDestAddress(destAddress);
				Address sourceAddress = submitSm.getSourceAddress();
				sourceAddress.setTon((byte) 5);
				sourceAddress.setNpi((byte) 1);
				submitSm.setSourceAddress(sourceAddress);
			}
			submitSm.removeSequenceNumber();
			submitSm.calculateAndSetCommandLength();

			try {
				submitResp = session0.submit(submitSm, 10000);
			} catch (SmppTimeoutException e) {
				LOGGER.error("{}-{}- {} 处理短信下行异常", Thread.currentThread().getName(), sendId, mbl, e);
			}
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
	private void handleMsgId(SubmitSmResp submitResp, String msgId) {
		if (submitResp == null) {
			return;
		}

		try {
			String messageId = submitResp.getMessageId();
			String[] split;
			if (msgId.contains("|")) {
				split = msgId.split("\\|");
			} else {
				split = new String[1];
				split[0] = msgId;
			}

			for (String curMsgId : split) {
				//更新缓存中的value
				Object msgVo = longOptRealMtSendRedisConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, curMsgId);
				longOptRealMtSendRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, curMsgId);

				Object msgVo1 = longOptRealMtSendRedisConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, messageId);
				if (msgVo1 == null) {
					longOptRealMtSendRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, messageId, msgVo);
				} else {
					MsgVo msg = (MsgVo) msgVo;
					String msgId2 = msg.getMsgId();

					MsgVo msg1 = (MsgVo) msgVo1;
					String msgId1 = msg1.getMsgId();

					msg1.setMsgId(msgId1 + "|" + msgId2);
					longOptRealMtSendRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, messageId, msg1);
				}
			}
		} catch (Exception e) {
			LOGGER.error("{}- 替换msgid异常", Thread.currentThread().getName(), e);
		}


	}


	/**
	 * 将发送失败的非opt短信放入到营销中，继续下一次发送
	 *
	 * @param submitSm 下行短信对象
	 */
	private void putSelfQueue(SubmitSm submitSm) {
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
				longOptRealMtSendRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_YX, submitSm);
				LOGGER.info("{} 长短信 将发送失败的非opt短信放入到营销中{}", Thread.currentThread().getName(), submitSm.toString());
				Thread.sleep(500);
			} else {
				LOGGER.error("systemid({}),senderid({}) 为OPT短信，丢弃该下行{}", sessionKey.getSystemId(), sessionKey.getSenderId(), submitSm.toString());
			}
		} catch (Exception e) {
			LOGGER.error("{} 长短信 将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName(), e);
		}
	}


}
