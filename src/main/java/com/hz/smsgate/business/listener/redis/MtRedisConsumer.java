package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.exception.SmppTimeoutException;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.ClientInit;
import com.hz.smsgate.business.pojo.MsgRelateVo;
import com.hz.smsgate.business.pojo.SenderIdVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * redis短信下行线程
 *
 * @author huangzhuo
 * @date 2019/10/17 15:38
 */
@Component
public class MtRedisConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(MtRedisConsumer.class);

	@Autowired
	public RedisUtil redisUtil;

	public static MtRedisConsumer mtRedisConsumer;

	@PostConstruct
	public void init() {
		mtRedisConsumer = this;
		mtRedisConsumer.redisUtil = this.redisUtil;
	}



	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}

		SubmitSm submitSm = new SubmitSm();
		LOGGER.info("{}-处理短信（redis）下行线程开始工作......", Thread.currentThread().getName());
		SubmitSmResp submitResp;
		Object obj;
		String sendId = "";
		Object tzObj;
		Object yxObj;
		while (true) {
			try {
				//redis对象为空休眠一秒
				if (mtRedisConsumer.redisUtil == null) {
					Thread.sleep(1000);
					continue;
				}

				obj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_SUBMIT_SM_OPT);

				if (obj == null) {
					//opt的短信发完后 处理通知的短信
					tzObj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_SUBMIT_SM_TZ);
					if (tzObj != null) {
						mtRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_OPT, tzObj);
						continue;
					}

					//通知的短信发完后 处理营销的短信
					yxObj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_SUBMIT_SM_YX);
					if (yxObj != null) {
						mtRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_OPT, yxObj);
						continue;
					}

					//三个通道都没消息 则休眠1s
					Thread.sleep(1000);
					continue;
				}

				submitSm = (SubmitSm) obj;
				//发送短信
				submitResp = realSend(submitSm);




				handleMsgId(submitResp, submitSm.getTempMsgId(),submitSm);

			} catch (Exception e) {
				LOGGER.error("{}-{}-{} 处理短信下行异常", Thread.currentThread().getName(), submitSm.getSystemId(), sendId, e);
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
            LOGGER.info("{}-读取到短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());

			//重组下行对象
			submitSm = PduUtils.rewriteSubmitSm(submitSm);

			mbl = submitSm.getDestAddress().getAddress();
			sendId = submitSm.getSourceAddress().getAddress();




			//获取客户端session
			SmppSession session0 = PduUtils.getSmppSession(submitSm);
			if (session0 == null) {
//				mtRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, submitSm.getTempMsgId());
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


			if (StaticValue.SYSTEMID_SA.equals(session0.getConfiguration().getSystemId())) {
				submitSm.setDataCoding(SmppConstants.DATA_CODING_DEFAULT);
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
	private void handleMsgId(SubmitSmResp submitResp, String msgId,SubmitSm submitSm) {
		if (submitResp == null) {
			return;
		}
		String messageId = submitResp.getMessageId();
		try {

			//更新缓存中的value
			Object msgVo = mtRedisConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, msgId);
			mtRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, msgId);



			if (msgVo != null && StringUtils.isNotBlank(messageId)) {
				mtRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, messageId, msgVo);
			} else {
				LOGGER.error("{}- {} -{}-{}短信记录异常，msgVo对象为空或者响应msgid为空，删除该条msgid: {} - {}", Thread.currentThread().getName(), submitSm.getSystemId(), submitSm.getSourceAddress().getAddress(), submitSm.getDestAddress().getAddress(), msgId, messageId);
			}


			mtRedisConsumer.redisUtil.lPush(SmppServerConstants.UPDATE_SUBMIT_SM, new MsgRelateVo(msgId, messageId));



		} catch (Exception e) {
			LOGGER.error("{}- {} -{}-{}替换msgid异常，msgVo对象为空或者响应msgid为空，删除该条msgid: {} - {}", Thread.currentThread().getName(), submitSm.getSystemId(), submitSm.getSourceAddress().getAddress(), submitSm.getDestAddress().getAddress(), msgId, messageId, e);
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
				LOGGER.error("{}  短短信 下行对象为空，将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName());
				return;
			}

			String senderId = submitSm.getSourceAddress().getAddress();
			SessionKey sessionKey = new SessionKey(submitSm.getSystemId(), senderId);
			if (!ClientInit.CHANNEL_OPT_LIST.contains(sessionKey)) {
				submitSm.removeSequenceNumber();
				submitSm.calculateAndSetCommandLength();
				mtRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_YX, submitSm);
				LOGGER.info("{} 短短信 将发送失败的非opt短信放入到营销中", Thread.currentThread().getName(), submitSm.toString());
				Thread.sleep(500);
			} else {
				LOGGER.error("systemid({}),senderid({}) 为OPT短信，丢弃该下行{}", sessionKey.getSystemId(), sessionKey.getSenderId(), submitSm.toString());
			}
		} catch (Exception e) {
			LOGGER.error("{} 短短信 将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName(), e);
		}
	}

}
