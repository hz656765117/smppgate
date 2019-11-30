package com.hz.smsgate.business.smpp.handler;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.exception.RecoverablePduException;
import com.hz.smsgate.base.smpp.exception.UnrecoverablePduException;
import com.hz.smsgate.base.smpp.pdu.*;
import com.hz.smsgate.base.smpp.pojo.PduAsyncResponse;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.base.utils.SmppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.ref.WeakReference;
import java.math.BigInteger;

//import com.hz.smsgate.business.listener.RptConsumer;


/**
 * @Author huangzhuo
 * @Date 2019/10/17 14:18
 */

@Component
public class ServerSmppSessionRedisHandler extends DefaultSmppSessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerSmppSessionRedisHandler.class);

    @Autowired
    public RedisUtil redisUtil;

    public static ServerSmppSessionRedisHandler serverSmppSessionRedisHandler;

    @PostConstruct
    public void init() {
        serverSmppSessionRedisHandler = this;
        serverSmppSessionRedisHandler.redisUtil = this.redisUtil;
    }


    private WeakReference<SmppSession> sessionRef;

    public ServerSmppSessionRedisHandler(SmppSession session) {
        this.sessionRef = new WeakReference<SmppSession>(session);
    }

    public ServerSmppSessionRedisHandler() {
        super();
    }

    public ServerSmppSessionRedisHandler(Logger logger) {
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
                        String msgid = SmppUtils.getMsgId();

                        submitResp.setMessageId(msgid);

                        String systemId = "";
                        SmppSession session = this.sessionRef.get();
                        if (session != null) {
                            systemId = session.getConfiguration().getSystemId();
                            submitSm.setSystemId(systemId);
                        }
                        logger.info("这是拆分短信,systemid{},msgid{},后缀为{}", systemId, msgid, SmppUtils.getSuffixKeyBySm(submitSm));

                        //临时流水id
                        String tempMsgId = submitResp.getMessageId() + SmppUtils.getSuffixKeyBySm(submitSm);
                        submitSm.setTempMsgId(tempMsgId);


                        try {
                            serverSmppSessionRedisHandler.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, tempMsgId, tempMsgId);
                            serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM, submitSm);
                        } catch (Exception e) {
                            logger.error("-----------长短信下行接收，加入队列异常。------------- {}", e);
                        }


                        String msgId16 = new BigInteger(msgid, 10).toString(16);
                        submitResp.setMessageId(msgId16);
                        return submitResp;
                    } else {

                        String msgid = SmppUtils.getMsgId();
                        submitSm.setTempMsgId(msgid);


                        String systemId = "";
                        SmppSession session = this.sessionRef.get();
                        if (session != null) {
                            systemId = session.getConfiguration().getSystemId();
                            submitSm.setSystemId(systemId);
                        }

                        logger.info("这是短短信,systemid为{},msgid为:{}", systemId, msgid);
                        try {
                            serverSmppSessionRedisHandler.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, msgid, msgid);
                            serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM, submitSm);
                        } catch (Exception e) {
                            logger.error("-----------短短信下行接收，加入队列异常。------------- {}", e);
                        }
                        String msgId16 = new BigInteger(msgid, 10).toString(16);
                        submitResp.setMessageId(msgId16);
                        submitResp.calculateAndSetCommandLength();
                        return submitResp;
                    }


                } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
                    return response;
                } else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_ENQUIRE_LINK) {
                    return response;
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


    public void putSelfQueue(SubmitSm submitSm) {
        String senderId = submitSm.getSourceAddress().getAddress();



        if (StaticValue.CHANNEL_YX_LIST.contains(senderId)) {
            serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_YX, submitSm);
        }else if (StaticValue.CHANNEL_TZ_LIST.contains(senderId)){
            serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_TZ, submitSm);
        }else if (StaticValue.CHANNEL_OPT_LIST.contains(senderId)){
            serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_OPT, submitSm);
        }else {
            serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_YX, submitSm);
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
