package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
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
		SubmitSm submitSm = new SubmitSm();
		LOGGER.info("{}-处理短信（redis）下行线程开始工作......", Thread.currentThread().getName());

		while (true) {

			String sendId = "";
			try {
				if (mtRedisConsumer.redisUtil != null) {
					Object obj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_SUBMIT_SM_OPT);
					if (obj != null) {

						submitSm = (SubmitSm) obj;
						sendId = submitSm.getSourceAddress().getAddress();
						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);
						LOGGER.info("{}-读取到短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());
						//获取客户端session
						SmppSession session0 = PduUtils.getSmppSession(submitSm);
						SubmitSmResp submitResp = session0.submit(submitSm, 10000);


						String messageId = submitResp.getMessageId();

						//更新缓存中的value
						mtRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, submitSm.getTempMsgId(), messageId);

					} else {
						//opt的短信发完后 处理通知的短信
						Object tzObj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_SUBMIT_SM_TZ);
						if (tzObj != null) {
							mtRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_OPT, tzObj);
							continue;
						}

						//通知的短信发完后 处理营销的短信
						Object yxObj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_SUBMIT_SM_YX);
						if (yxObj != null) {
							mtRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_OPT, yxObj);
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
				LOGGER.error("{}-{}处理短信下行异常", Thread.currentThread().getName(), sendId, e);
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
				mtRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_YX, submitSm);
				LOGGER.info("{} 将发送失败的非opt短信放入到营销中", Thread.currentThread().getName(), submitSm.toString());
				Thread.sleep(500);
			}
		} catch (Exception e) {
			LOGGER.error("{} 将发送失败的非opt短信放入到营销中异常", Thread.currentThread().getName(), e);
		}
	}

}
