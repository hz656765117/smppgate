package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.RptConsumer;
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
		SubmitSm submitSm;


		while (true) {

			try {
				if (longRealMtSendRedisConsumer.redisUtil != null) {
					Object obj = longRealMtSendRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_REL_LONG_SUBMIT_SM_SEND);
					if (obj != null) {
						submitSm = (SubmitSm) obj;

						String[] tempMsgIds = submitSm.getTempMsgId().split("\\|");
						//获取客户端session
						SmppSession session0 = PduUtils.getSmppSession(submitSm);
						SubmitSmResp submitResp = session0.submit(submitSm, 10000);
						String messageId = submitResp.getMessageId();

						//更新缓存中的value
						for (String key : tempMsgIds) {
							//0 je   1 redis
							if ("1".equals(StaticValue.TYPE)) {
								longRealMtSendRedisConsumer.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, key, messageId);
							}else {
								RptConsumer.CACHE_MAP.put(key, messageId);
							}

						}
					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-长短信分段下发异常", Thread.currentThread().getName(), e);
			}

		}

	}


}
