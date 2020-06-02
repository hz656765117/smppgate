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
import com.hz.smsgate.business.pojo.MsgVo;
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
public class WebCmMtRedisOptConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(WebCmMtRedisOptConsumer.class);

	@Autowired
	public RedisUtil redisUtil;

	public static WebCmMtRedisOptConsumer mtRedisOptConsumer;

	@PostConstruct
	public void init() {
		mtRedisOptConsumer = this;
		mtRedisOptConsumer.redisUtil = this.redisUtil;
	}



	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}

		SubmitSm submitSm = new SubmitSm();
		LOGGER.info("{}-处理CM短信（redis）OPT下行线程开始工作......", Thread.currentThread().getName());
		SubmitSmResp submitResp;
		Object obj;
		String sendId = "";
		Object tzObj;
		Object yxObj;
		while (true) {
			try {
				//redis对象为空休眠一秒
				if (mtRedisOptConsumer.redisUtil == null) {
					Thread.sleep(1000);
					continue;
				}
				obj = mtRedisOptConsumer.redisUtil.rPop(SmppServerConstants.WEB_CM_SUBMIT_SM_OPT);

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
	private void handleMsgId(SubmitSmResp submitResp, String msgId) {
		if (submitResp == null) {
			return;
		}

		try {
			String messageId = submitResp.getMessageId();
			//更新缓存中的value
			Object msgVo = mtRedisOptConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, msgId);
			mtRedisOptConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, msgId);
			mtRedisOptConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, messageId, msgVo);
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
				LOGGER.error("{}  短短信 下行对象为空，将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName());
				return;
			}

			String senderId = submitSm.getSourceAddress().getAddress();
			SessionKey sessionKey = new SessionKey(submitSm.getSystemId(), senderId);
			if (!ClientInit.CHANNEL_OPT_LIST.contains(sessionKey)) {
				submitSm.removeSequenceNumber();
				submitSm.calculateAndSetCommandLength();
				mtRedisOptConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_YX, submitSm);
				LOGGER.info("{} 短短信 将发送失败的非opt短信放入到营销中", Thread.currentThread().getName(), submitSm.toString());
				Thread.sleep(500);
			} else {
				Object obj = mtRedisOptConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, submitSm.getTempMsgId());
				if (obj == null) {
					return;
				}
				MsgVo msgVo = (MsgVo) obj;
				Integer sendSize = msgVo.getSendSize();
				if (sendSize >= 4) {
					mtRedisOptConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, submitSm.getTempMsgId());
					LOGGER.error("systemid({}),senderid({}) 为OPT短信,且已重发三次，丢弃该下行{}", sessionKey.getSystemId(), sessionKey.getSenderId(), submitSm.toString());
					return;
				}
				sendSize = sendSize + 1;
				msgVo.setSendSize(sendSize);
				mtRedisOptConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, submitSm.getTempMsgId(), msgVo);
				mtRedisOptConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_OPT, submitSm);
				LOGGER.info("{}  WEBD短短信OPT短信 将发送失败的opt短信放入到OPT中{}", Thread.currentThread().getName(), submitSm.toString());
			}
		} catch (Exception e) {
			LOGGER.error("{} 短短信 将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName(), e);
		}
	}

}
