package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.RptConsumer;
import com.hz.smsgate.business.smpp.handler.ServerSmppSessionRedisHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;


/**
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
		System.out.println("lll");
	}

	@Override
	public void run() {
		SubmitSm submitSm;


		while (true) {

			String sendId = "";
			try {
				if (mtRedisConsumer.redisUtil != null) {
					Object obj = mtRedisConsumer.redisUtil.rPop("submitSm");
					if (obj != null) {

						submitSm = (SubmitSm) obj;
						sendId = submitSm.getSourceAddress().getAddress();

						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);

						//获取客户端session
						SmppSession session0 = PduUtils.getSmppSession(submitSm);

						LOGGER.info("{}-读取到状态报告信息{}", Thread.currentThread().getName(), submitSm.toString());
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
						RptConsumer.CACHE_MAP.put(submitSm.getTempMsgId(), messageId);

						int msgLen = messageId.length();
						if (msgLen > 19) {
							messageId = messageId.substring(msgLen - 19, msgLen);
							submitResp.setMessageId(messageId);
							submitResp.setCommandLength(submitResp.getCommandLength() - (msgLen - 19));
						}

						BlockingQueue<Object> submitRespQueue = BDBStoredMapFactoryImpl.INS.getQueue("submitResp", "submitResp");
						submitRespQueue.put(submitResp);


					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-{}处理短信状态报告转发异常", Thread.currentThread().getName(), sendId, e);
			}

		}

	}


}
