package com.hz.smsgate.business.smpp.handler;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2014 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.exception.RecoverablePduException;
import com.hz.smsgate.base.smpp.exception.UnrecoverablePduException;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pdu.Pdu;
import com.hz.smsgate.base.smpp.pdu.PduRequest;
import com.hz.smsgate.base.smpp.pdu.PduResponse;
import com.hz.smsgate.base.smpp.pojo.PduAsyncResponse;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.DeliveryReceipt;
import com.hz.smsgate.business.utils.RedisUtil;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.ref.WeakReference;

@Component
public class Client1SmppSessionHandler extends DefaultSmppSessionHandler {

	private WeakReference<SmppSession> sessionRef;

	@Autowired
	public RedisUtil redisUtil;

	public static Client1SmppSessionHandler client1SmppSessionHandler;

	@PostConstruct
	public void init() {
		client1SmppSessionHandler = this;
		client1SmppSessionHandler.redisUtil = this.redisUtil;
	}


	@Override
	public void setSmppSession(SmppSession session) {
		this.sessionRef = new WeakReference<>(session);
	}

	private static final Logger logger = LoggerFactory.getLogger(Client1SmppSessionHandler.class);

	public Client1SmppSessionHandler() {
		super();
	}

	public Client1SmppSessionHandler(Logger logger) {
		super(logger);
	}

	@Override
	public String lookupResultMessage(int commandStatus) {
		return super.lookupResultMessage(commandStatus);
	}

	@Override
	public String lookupTlvTagName(short tag) {
		return super.lookupTlvTagName(tag);
	}

	@Override
	public void fireChannelUnexpectedlyClosed() {
		super.fireChannelUnexpectedlyClosed();
	}

	@Override
	public PduResponse firePduRequestReceived(PduRequest pduRequest) {
		PduResponse response = pduRequest.createResponse();
		SmppSession session = this.sessionRef.get();
//		DefaultSmppSession defaultSmppSession = (DefaultSmppSession) session;

		String systemId = "";
		if (session != null) {
			systemId = session.getConfiguration().getSystemId();
		}

		try {
			if (pduRequest.isRequest()) {
				switch (pduRequest.getCommandId()) {
					case SmppConstants.CMD_ID_SUBMIT_SM:
						logger.error("----------客户端怎么会接到短信请求呢 ---------------");
						break;
					case SmppConstants.CMD_ID_DELIVER_SM:
						DeliverSm deliverSm = (DeliverSm) pduRequest;

						deliverSm.setSystemId(systemId);
						putRedisCache(deliverSm);

						break;
					case SmppConstants.CMD_ID_DATA_SM:
						return response;
					case SmppConstants.CMD_ID_ENQUIRE_LINK:
						logger.info("---------客户端{}也会接收心跳请求----------{}", systemId, pduRequest);
						return response;
					case SmppConstants.CMD_ID_UNBIND:
						logger.info("---------客户端{}被解绑了----------{}", systemId, pduRequest);
						return response;
					default:
						logger.error("----------客户端{}接收 未知异常。---------------{}", systemId, pduRequest);
						return response;

				}
			}

		} catch (Exception e) {
			logger.error("--------客户端处理异常---------", e);
		}

		// do any logic here

		return response;
	}

	/**
	 * 将状态报告缓存到对应的redis中，交由不同的服务端处理
	 *
	 * @param deliverSm
	 */
	public static void putRedisCache(DeliverSm deliverSm) {
		try {

			//获取状态报告msgid
			String str = new String(deliverSm.getShortMessage());
			DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(str, DateTimeZone.UTC);

			String messageId = deliveryReceipt.getMessageId();
			Object obj = client1SmppSessionHandler.redisUtil.hmGet(SmppServerConstants.CM_MSGID_CACHE, messageId);

			//判断msgid是否在cm的msgid缓存中存在
			if (obj != null) {
				client1SmppSessionHandler.redisUtil.lPush(SmppServerConstants.CM_DELIVER_SM, deliverSm);
			} else {
				client1SmppSessionHandler.redisUtil.lPush(SmppServerConstants.WEB_DELIVER_SM, deliverSm);
			}

		} catch (Exception e) {
			logger.error("{}-处理短信状态报告内容解析异常", Thread.currentThread().getName(), e);
			return;
		}

	}


	@Override
	public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
		super.fireExpectedPduResponseReceived(pduAsyncResponse);
	}

	@Override
	public void fireUnexpectedPduResponseReceived(PduResponse pduResponse) {
		super.fireUnexpectedPduResponseReceived(pduResponse);
	}

	@Override
	public void fireUnrecoverablePduException(UnrecoverablePduException e) {
		super.fireUnrecoverablePduException(e);
	}

	@Override
	public void fireRecoverablePduException(RecoverablePduException e) {
		super.fireRecoverablePduException(e);
	}

	@Override
	public void fireUnknownThrowable(Throwable t) {
		super.fireUnknownThrowable(t);
	}

	@Override
	public void firePduRequestExpired(PduRequest pduRequest) {
		super.firePduRequestExpired(pduRequest);
	}

	@Override
	public boolean firePduReceived(Pdu pdu) {
		return super.firePduReceived(pdu);
	}

	@Override
	public boolean firePduDispatch(Pdu pdu) {
		return super.firePduDispatch(pdu);
	}
}
