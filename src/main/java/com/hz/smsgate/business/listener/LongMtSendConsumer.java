package com.hz.smsgate.business.listener;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.smsgate.base.utils.PduUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


/**
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
public class LongMtSendConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongMtSendConsumer.class);


	public static final List<SubmitSm> sendlist = new LinkedList<>();

	@Override
	public void run() {
		SubmitSm submitSm;


		while (true) {

			BlockingQueue<Object> queue = null;
			try {
				queue = BDBStoredMapFactoryImpl.INS.getQueue("longSubmitSmSend", "longSubmitSmSend");
			} catch (Exception e) {
				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
			}

			try {
				if (queue != null) {
					Object obj = queue.poll();
					if (obj != null) {
						submitSm = (SubmitSm) obj;

						//重组下行对象
						submitSm = PduUtils.rewriteSubmitSm(submitSm);

						String[] tempMsgIds = submitSm.getTempMsgId().split("\\|");


						//获取客户端session
						SmppSession session0 = PduUtils.getSmppSession(submitSm);


						LOGGER.info("{}-读取到长短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());
						SubmitSmResp submitResp = session0.submit(submitSm, 10000);

						String sendId = submitSm.getSourceAddress().getAddress();
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
						for (String key : tempMsgIds) {
							RptConsumer.CACHE_MAP.put(key, messageId);
						}

					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-处理短信状态报告转发异常", Thread.currentThread().getName(), e);
			}

		}

	}


}
