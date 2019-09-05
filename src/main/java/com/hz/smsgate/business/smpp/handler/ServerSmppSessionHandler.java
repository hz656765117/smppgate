package com.hz.smsgate.business.smpp.handler;

import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.exception.RecoverablePduException;
import com.hz.smsgate.base.smpp.exception.UnrecoverablePduException;
import com.hz.smsgate.base.smpp.pdu.*;
import com.hz.smsgate.base.smpp.pojo.PduAsyncResponse;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.smsgate.base.smpp.utils.PrimaryGenerater;
import com.hz.smsgate.business.listener.ClientInit;
import com.hz.smsgate.business.listener.LongMtConsumer;
import com.hz.smsgate.business.listener.RptConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

/**
 * @Auther: huangzhuo
 * @Date: 2019/8/28 10:33
 * @Description:
 */
public class ServerSmppSessionHandler extends DefaultSmppSessionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ServerSmppSessionHandler.class);

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
				if (pduRequest.getCommandId() == SmppConstants.CMD_ID_SUBMIT_SM) {
					SubmitSmResp submitResp = (SubmitSmResp) response;
					SubmitSm submitSm = (SubmitSm) pduRequest;
					//通道替换
					submitSm = PduUtil.rewriteSmSourceAddress(submitSm);


					byte[] shortMessage = submitSm.getShortMessage();
					if (shortMessage[0] == 5 && shortMessage[1] == 0 && shortMessage[2] == 3) {
						String msgid = LongMtConsumer.getMsgId();
						logger.info("这是拆分短信,msgid{},后缀为{}", msgid, LongMtConsumer.getSuffixKeyBySm(submitSm));
						submitResp.setMessageId(msgid);

						//临时流水id
						String tempMsgId = submitResp.getMessageId() + LongMtConsumer.getSuffixKeyBySm(submitSm);

						RptConsumer.CACHE_MAP.put(tempMsgId, tempMsgId);

						submitSm.setTempMsgId(tempMsgId);
						try {
							BlockingQueue<Object> queue = BDBStoredMapFactoryImpl.INS.getQueue("longSubmitSm", "longSubmitSm");
							queue.put(submitSm);
						} catch (Exception e) {
							logger.error("-----------长短信下行接收，加入队列异常。------------- {}", e);
						}


						String msgId16 = new BigInteger(msgid,10).toString(16);
						submitResp.setMessageId(msgId16);
						return submitResp;
					} else {


						String msgid = LongMtConsumer.getMsgId();
						submitSm.setTempMsgId(msgid);
						logger.info("这是短短信,msgid为:{},后缀为{}", msgid);
						RptConsumer.CACHE_MAP.put(msgid, msgid);

						try {
							BlockingQueue<Object> queue = BDBStoredMapFactoryImpl.INS.getQueue("submitSm", "submitSm");
							queue.put(submitSm);
						} catch (Exception e) {
							logger.error("-----------短短信下行接收，加入队列异常。------------- {}", e);
						}
						while (true) {
							BlockingQueue<Object> submitRespQueue = null;
							try {
								submitRespQueue = BDBStoredMapFactoryImpl.INS.getQueue("submitResp", "submitResp");
								if (submitRespQueue != null) {
									Object obj = submitRespQueue.poll();
									if (obj != null) {
										SubmitSmResp submitSmResp = (SubmitSmResp) obj;
										String msgId16 = new BigInteger(msgid,10).toString(16);
										submitSmResp.setMessageId(msgId16);
										submitSmResp.calculateAndSetCommandLength();
										return submitSmResp;
									}
								}
							} catch (Exception e) {
								logger.error("短信下行响应异常 {}", e);
							}
						}

					}


				} else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
					return response;
				} else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_ENQUIRE_LINK) {
					EnquireLinkResp enquireLinkResp = session0.enquireLink(new EnquireLink(), 10000);
					return enquireLinkResp;
				} else {
					return response;
				}
			} else {
				if (pduRequest.getCommandId() == SmppConstants.CMD_ID_SUBMIT_SM_RESP) {
					return response;
				} else {
					return response;
				}

			}
		} catch (Exception e) {
			return response;
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
