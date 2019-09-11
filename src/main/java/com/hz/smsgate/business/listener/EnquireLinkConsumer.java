package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.pdu.EnquireLink;
import com.hz.smsgate.base.smpp.pdu.EnquireLinkResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @author huangzhuo
 * @date 2019/9/11 14:27
 */
public class EnquireLinkConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(EnquireLinkConsumer.class);

	@Override
	public void run() {


		while (true) {

			try {
				Map<String, SmppSession> sessionMap = ClientInit.sessionMap;
				if (sessionMap != null && sessionMap.size() > 0) {
					for (Map.Entry<String, SmppSession> entry : sessionMap.entrySet()) {
						SmppSession session0 = entry.getValue();
						try {
							EnquireLinkResp enquireLinkResp = session0.enquireLink(new EnquireLink(), 10000);
						} catch (Exception e) {
							LOGGER.error("{}-{}心跳异常异常", Thread.currentThread().getName(), entry.getKey(), e);
						}

					}
					Thread.sleep(StaticValue.ENQUIRE_LINK_TIME);
				} else {
					Thread.sleep(5000);
				}


			} catch (Exception e) {
				LOGGER.error("{}-处理短信状态报告转发异常", Thread.currentThread().getName(), e);
			}

		}

	}


}
