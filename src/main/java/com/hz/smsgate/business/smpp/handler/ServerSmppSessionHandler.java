package com.hz.smsgate.business.smpp.handler;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.exception.RecoverablePduException;
import com.hz.smsgate.base.smpp.exception.UnrecoverablePduException;
import com.hz.smsgate.base.smpp.pdu.*;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.PduAsyncResponse;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.business.listener.ClientInit;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;

/**
 * @Auther: huangzhuo
 * @Date: 2019/8/28 10:33
 * @Description:
 */
public class ServerSmppSessionHandler extends DefaultSmppSessionHandler {


	private WeakReference<SmppSession> sessionRef;

	public ServerSmppSessionHandler(SmppSession session) {
		this.sessionRef = new WeakReference<SmppSession>(session);
	}

	public ServerSmppSessionHandler() {
		super();
	}

	public ServerSmppSessionHandler(Logger logger) {
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
		SmppSession session = sessionRef.get();
		PduResponse response = pduRequest.createResponse();
		SmppSession session0 = ClientInit.session0;
		if (session0 == null) {
			try {
				session0 = ClientInit.clientBootstrap.bind(ClientInit.config0, ClientInit.sessionHandler);
			} catch (Exception e) {

			}

		}
		// mimic how long processing could take on a slower smsc
		try {
			if (pduRequest.isRequest()) {
				switch (pduRequest.getCommandId()) {
					case SmppConstants.CMD_ID_SUBMIT_SM:
//						String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
//						byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM);
//						SubmitSm submit0 = new SubmitSm();
//						submit0.setSourceAddress(new Address((byte) 0x03, (byte) 0x00, "40404"));
//						submit0.setDestAddress(new Address((byte) 0x01, (byte) 0x01, "44555519205"));

//						submit0.setShortMessage(textBytes);
						SubmitSm submitSm = (SubmitSm) pduRequest;
						try {

							SubmitSmResp submitResp = session0.submit(submitSm, 10000);
							submitResp.setSequenceNumber(response.getSequenceNumber());
							String messageId = submitResp.getMessageId();
							System.out.println("--------messageId为" + messageId);
							int msgLen = messageId.length();
							if (msgLen > 19) {
								messageId = messageId.substring(0, 19);
								submitResp.setMessageId(messageId);
								System.out.println("--------messageId为" + messageId);
								submitResp.setCommandLength(submitResp.getCommandLength() - (msgLen - 19));
							}
							response = submitResp;
						} catch (Exception e) {

						}
						System.out.println(pduRequest.getCommandId());

						break;
					case SmppConstants.CMD_ID_DELIVER_SM:
						System.out.println(pduRequest.getCommandId());
						break;
					case SmppConstants.CMD_ID_DATA_SM:
						System.out.println(pduRequest.getCommandId());
						break;
					case SmppConstants.CMD_ID_ENQUIRE_LINK:
						EnquireLinkResp enquireLinkResp = session0.enquireLink(new EnquireLink(), 10000);
						enquireLinkResp.setSequenceNumber(response.getSequenceNumber());
						response = enquireLinkResp;
						System.out.println(pduRequest.getCommandId());
						break;
					default:
						System.out.println("llllllllll");
				}
			}
			//Thread.sleep(50);
		} catch (Exception e) {
		}

		return response;
//		return super.firePduRequestReceived(pduRequest);
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
