package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.je.RptConsumer;
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
		SubmitSm submitSm = new SubmitSm();
		LOGGER.info("{}-长短信（redis）真实发送线程开始工作......", Thread.currentThread().getName());

		while (true) {

			try {
				if (longRealMtSendRedisConsumer.redisUtil != null) {
					Object obj = longRealMtSendRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND_OPT);
					if (obj != null) {
						submitSm = (SubmitSm) obj;
						LOGGER.info("{}-读取到长短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());

						String[] tempMsgIds = submitSm.getTempMsgId().split("\\|");

						submitSm.removeSequenceNumber();
						submitSm.calculateAndSetCommandLength();

						//获取客户端session
						SmppSession session0 = PduUtils.getSmppSession(submitSm);
						SubmitSmResp submitResp = session0.submit(submitSm, 15000);
						String messageId = submitResp.getMessageId();

						//更新缓存中的value
						for (String key : tempMsgIds) {
							//0 je   1 redis
							if ("1".equals(StaticValue.TYPE)) {
								longRealMtSendRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, key, messageId);
							} else {
								RptConsumer.CACHE_MAP.put(key, messageId);
							}

						}
					} else {

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
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				putSelfQueue(submitSm);
				LOGGER.error("{}-长短信分段下发异常", Thread.currentThread().getName(), e);
			}

		}

	}


	/**
	 * 将发送失败的非opt短信放入到营销中，继续下一次发送
	 *
	 * @param submitSm 下行短信对象
	 */
	public void putSelfQueue(SubmitSm submitSm) {
		try {
			String senderId = submitSm.getSourceAddress().getAddress();
			if (!StaticValue.CHANNEL_OPT_LIST.contains(senderId)) {
				submitSm.removeSequenceNumber();
				submitSm.calculateAndSetCommandLength();
				longRealMtSendRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_YX, submitSm);
				LOGGER.info("{} 将发送失败的非opt短信放入到营销中", Thread.currentThread().getName(), submitSm.toString());
				Thread.sleep(500);
			}
		} catch (Exception e) {
			LOGGER.error("{} 将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName(), e);
		}
	}


}
