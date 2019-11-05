package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;


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
		SubmitSm submitSm;

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			LOGGER.error("{}-处理短信（redis）-cm下行线程启动异常", Thread.currentThread().getName(), e);
		}
		LOGGER.info("{}-处理短信（redis）-cm下行线程开始工作......", Thread.currentThread().getName());

		while (true) {

			String sendId = "";
			try {
				if (mtRedisConsumer.redisUtil != null) {
					Object obj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.CM_SUBMIT_SM);
					if (obj != null) {

						submitSm = (SubmitSm) obj;
						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);

						sendId = submitSm.getSourceAddress().getAddress();

						//获取客户端session
						SmppSession session0 = PduUtils.getSmppSession(submitSm);

						LOGGER.info("{}-读取到短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());
						SubmitSmResp submitResp = session0.submit(submitSm, 10000);

						WGParams wgParams = StaticValue.CHANNL_SP_REL.get(sendId);
						if (wgParams != null) {
							BlockingQueue<Object> syncSubmitQueue = BDBStoredMapFactoryImpl.INS.getQueue("syncSubmit", "syncSubmit");
							wgParams.setDas(submitSm.getDestAddress().getAddress());
							String sm = new String(submitSm.getShortMessage());
							wgParams.setSm(sm);
							syncSubmitQueue.put(wgParams);
						}
						String messageId = submitResp.getMessageId();
						//更新缓存中的value
						mtRedisConsumer.redisUtil.hmSet(SmppServerConstants.CM_MSGID_CACHE, submitSm.getTempMsgId(), messageId);

					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-{}处理短信下行异常", Thread.currentThread().getName(), sendId, e);
			}

		}

	}


}
