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


import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.exception.RecoverablePduException;
import com.hz.smsgate.base.smpp.exception.UnrecoverablePduException;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pdu.Pdu;
import com.hz.smsgate.base.smpp.pdu.PduRequest;
import com.hz.smsgate.base.smpp.pdu.PduResponse;
import com.hz.smsgate.base.smpp.pojo.PduAsyncResponse;
import com.hz.smsgate.base.smpp.utils.DeliveryReceipt;
import com.hz.smsgate.business.smpp.impl.DefaultSmppServer;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;


public class Client1SmppSessionHandler extends DefaultSmppSessionHandler {

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

		try {
			if (pduRequest.isRequest()) {
				switch (pduRequest.getCommandId()) {
					case SmppConstants.CMD_ID_SUBMIT_SM:
						DefaultSmppServer.smppSession.sendRequestPdu(pduRequest, 3000, true);
						break;
					case SmppConstants.CMD_ID_DELIVER_SM:
						DeliverSm deliverSm = (DeliverSm) pduRequest;
//						if (deliverSm.getEsmClass()!=0){
//							byte[] shortMessage = deliverSm.getShortMessage();
//							int len1 = shortMessage.length;
//							String str = new String(shortMessage);
//							DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(str, DateTimeZone.UTC);
//
//							DateTime dateTime = new DateTime();
//							deliveryReceipt.setDoneDate(dateTime);
//							deliveryReceipt.setSubmitDate(dateTime);
//
//							String messageId = deliveryReceipt.getMessageId();
//							System.out.println("-------- deliveryReceipt messageId为" + messageId);
////							int msgLen = messageId.length();
////							if (msgLen > 19) {
////								messageId = messageId.substring(0, 19);
////								deliveryReceipt.setMessageId(messageId);
////								System.out.println("--------deliveryReceipt messageId为" + messageId);
////							}
//
//
//							byte[] bytes = deliveryReceipt.toShortMessage().getBytes();
//							int len2 = bytes.length;
//							deliverSm.setCommandLength(deliverSm.getCommandLength() + (len2 - len1));
//							deliverSm.setShortMessage(bytes);
//
//
//
//							System.out.println(str);
//						}

						try {
							BlockingQueue<Object> queue = BDBStoredMapFactoryImpl.INS.getQueue("rptrvok", "rptrvok");
							queue.put(deliverSm);
						} catch (Exception e) {
							System.out.println("--------------------状态报告接收，加入队列异常。------------------------------------------------------");
						}


//						DefaultSmppServer.smppSession.sendRequestPdu(deliverSm, 3000, true);
						break;
					case SmppConstants.CMD_ID_DATA_SM:
						DefaultSmppServer.smppSession.sendRequestPdu(pduRequest, 3000, true);
						break;
					case SmppConstants.CMD_ID_ENQUIRE_LINK:
						DefaultSmppServer.smppSession.sendRequestPdu(pduRequest, 3000, true);
						break;
					default:
						DefaultSmppServer.smppSession.sendRequestPdu(pduRequest, 3000, true);
						System.out.println("llllllllll");
				}
			}
			System.out.println("==================1025285137102798860=========10252851371027988607===============================");

		} catch (Exception e) {
			System.out.println("---------------------------------+"+e.toString()+"1--"+e.getStackTrace()+"-----------转发异常----------------------------------------------"+e.getMessage());
		}

		// do any logic here

		return response;
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
