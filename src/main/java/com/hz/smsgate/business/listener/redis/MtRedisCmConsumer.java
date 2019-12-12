package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.emp.pojo.WGParams;
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
 * cm redis短信下行线程
 *
 * @author huangzhuo
 * @date 2019/10/17 15:38
 */
@Component
public class MtRedisCmConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(MtRedisCmConsumer.class);

	@Autowired
	public RedisUtil redisUtil;

	public static MtRedisCmConsumer mtRedisConsumer;

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
		SubmitSm submitSm;
		LOGGER.info("{}-处理短信（redis）-cm下行线程开始工作......", Thread.currentThread().getName());
		SubmitSmResp submitResp;
		while (true) {
			try {
				//redis对象为空休眠一秒
				if (mtRedisConsumer.redisUtil == null) {
					Thread.sleep(1000);
					continue;
				}
				Object obj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.CM_SUBMIT_SM_OPT);
				//下行对象为空，从其他队列中获取，获取不到休眠半秒
				if (obj == null) {
					//opt的短信发完后 处理通知的短信
					Object tzObj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.CM_SUBMIT_SM_TZ);
					if (tzObj != null) {
						mtRedisConsumer.redisUtil.lPush(SmppServerConstants.CM_SUBMIT_SM_OPT, tzObj);
						continue;
					}

					//通知的短信发完后 处理营销的短信
					Object yxObj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.CM_SUBMIT_SM_YX);
					if (yxObj != null) {
						mtRedisConsumer.redisUtil.lPush(SmppServerConstants.CM_SUBMIT_SM_OPT, yxObj);
						continue;
					}

					//三个通道都没消息 则休眠1s
					Thread.sleep(1000);
					continue;
				}

				submitSm = (SubmitSm) obj;
				//发送短信
				submitResp = realSend(submitSm);

				if (submitResp != null) {
					//发送到网关
					sendToWg(submitSm);
				}


				//处理msgid
				handleMsgId(submitResp, submitSm.getTempMsgId());


			} catch (Exception e) {
				LOGGER.error("{}- 处理短信下行异常", Thread.currentThread().getName(), e);
			}


		}

	}



	public void sendToWg(SubmitSm submitSm) {
		SessionKey sessionKey = new SessionKey();
		Object obj = mtRedisConsumer.redisUtil.hmGet(SmppServerConstants.CM_MSGID_CACHE, submitSm.getTempMsgId());
		if (obj != null) {
			MsgVo msgVo = (MsgVo) obj;
			sessionKey.setSystemId(msgVo.getSmppUser());
			sessionKey.setSenderId(msgVo.getSmppPwd());
		}

		WGParams wgParams = ClientInit.CHANNL_SP_REL.get(sessionKey);
		if (wgParams != null) {
			wgParams.setDas(submitSm.getDestAddress().getAddress());
			String sm = new String(submitSm.getShortMessage());
			wgParams.setSm(sm);
			mtRedisConsumer.redisUtil.lPush(SmppServerConstants.SYNC_SUBMIT, wgParams);
		} else {
			LOGGER.error("{}- {} -{}-{}短信记录异常，未能获取到sp账号", Thread.currentThread().getName(), submitSm.getSystemId(), submitSm.getSourceAddress().getAddress(),submitSm.getDestAddress().getAddress());
		}

	}


	public void handleMsgId(SubmitSmResp submitResp, String msgId) {
		if (submitResp == null) {
			return;
		}
		String messageId = submitResp.getMessageId();
		//更新缓存中的value
		Object msgVo = mtRedisConsumer.redisUtil.hmGet(SmppServerConstants.CM_MSGID_CACHE, msgId);
		mtRedisConsumer.redisUtil.hmRemove(SmppServerConstants.CM_MSGID_CACHE, msgId);
		mtRedisConsumer.redisUtil.hmSet(SmppServerConstants.CM_MSGID_CACHE, messageId, msgVo);
	}


	public SubmitSmResp realSend(SubmitSm submitSm) {
		SubmitSmResp submitResp = null;
		String sendId = "";
		String mbl = "";

		try {
			//重组下行对象
			submitSm = PduUtils.rewriteSubmitSm(submitSm);
			sendId = submitSm.getSourceAddress().getAddress();
			mbl = submitSm.getDestAddress().getAddress();

			LOGGER.info("{}-读取到短信下行信息 手机号码（{}），短信内容（{}），短信内容（{}）", Thread.currentThread().getName(), submitSm.getDestAddress().getAddress(), new String(submitSm.getShortMessage(), "UTF-8"), new String(submitSm.getShortMessage(), "GBK"));
			//获取客户端session
			SmppSession session0 = PduUtils.getSmppSession(submitSm);

			if (session0 == null) {
//				mtRedisConsumer.redisUtil.hmRemove(SmppServerConstants.CM_MSGID_CACHE, submitSm.getTempMsgId());
				LOGGER.error("systemid({}),senderid({}),mbl（{}）获取客户端连接异常，丢弃该下行", submitSm.getSystemId(), sendId, mbl);
				return submitResp;
			}

			submitSm.removeSequenceNumber();
			submitSm.calculateAndSetCommandLength();


			submitResp = session0.submit(submitSm, 10000);
		} catch (Exception e) {
			putSelfQueue(submitSm);
			LOGGER.error("{}-{}- {} 处理短信下行异常", Thread.currentThread().getName(), sendId, mbl, e);
		}

		return submitResp;
	}


	/**
	 * 将发送失败的非opt短信放入到营销中，继续下一次发送
	 *
	 * @param submitSm 下行短信对象
	 */
	public void putSelfQueue(SubmitSm submitSm) {
		try {
			if (submitSm.getSourceAddress() == null) {
				LOGGER.error("{} CM短短信 下行对象为空，将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName());
				return;
			}
			String senderId = submitSm.getSourceAddress().getAddress();
			SessionKey sessionKey = new SessionKey(submitSm.getSystemId(), senderId);
			if (!ClientInit.CHANNEL_OPT_LIST.contains(sessionKey)) {
				submitSm.removeSequenceNumber();
				submitSm.calculateAndSetCommandLength();
				mtRedisConsumer.redisUtil.lPush(SmppServerConstants.CM_SUBMIT_SM_YX, submitSm);
				LOGGER.info("{}  CM短短信 将发送失败的非opt短信放入到营销中", Thread.currentThread().getName(), submitSm.toString());
				Thread.sleep(500);
			} else {
				mtRedisConsumer.redisUtil.hmRemove(SmppServerConstants.CM_MSGID_CACHE, submitSm.getTempMsgId());
				LOGGER.error("systemid({}),senderid({}) 为OPT短信，丢弃该下行{}", sessionKey.getSystemId(), sessionKey.getSenderId(), submitSm.toString());
			}
		} catch (Exception e) {
			LOGGER.error("{}  CM短短信 将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName(), e);
		}
	}


}
