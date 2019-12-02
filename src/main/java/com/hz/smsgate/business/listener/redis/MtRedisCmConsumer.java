package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


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
        LOGGER.info("{}-处理短信（redis）-cm下行线程开始工作......", Thread.currentThread().getName());

        while (true) {
            SessionKey sessionKey;
            String sendId = "";
            String mbl = "";
            try {
                if (mtRedisConsumer.redisUtil != null) {
                    Object obj = mtRedisConsumer.redisUtil.rPop(SmppServerConstants.CM_SUBMIT_SM);
                    if (obj != null) {

                        submitSm = (SubmitSm) obj;
                        //重组下行对象
                        submitSm = PduUtils.rewriteSubmitSm(submitSm);

                        sendId = submitSm.getSourceAddress().getAddress();
                        mbl = submitSm.getDestAddress().getAddress();

                        LOGGER.info("{}-读取到短信下行信息{}", Thread.currentThread().getName(), submitSm.toString());
                        //获取客户端session
                        SmppSession session0 = PduUtils.getSmppSession(submitSm);

                        SubmitSmResp submitResp = session0.submit(submitSm, 10000);

                        String messageId = submitResp.getMessageId();
                        //更新缓存中的value
                        mtRedisConsumer.redisUtil.hmSet(SmppServerConstants.CM_MSGID_CACHE, submitSm.getTempMsgId(), messageId);

                        sessionKey = new SessionKey();
                        sessionKey.setSystemId(submitSm.getSystemId());
                        sessionKey.setSenderId(sendId);
                        WGParams wgParams = StaticValue.CHANNL_SP_REL.get(sessionKey);
                        if (wgParams != null) {
                            wgParams.setDas(submitSm.getDestAddress().getAddress());
                            String sm = new String(submitSm.getShortMessage());
                            wgParams.setSm(sm);
                            mtRedisConsumer.redisUtil.lPush(SmppServerConstants.SYNC_SUBMIT, wgParams);
                        } else {
                            LOGGER.error("{}- {} -{}短信记录异常，未能获取到sp账号", Thread.currentThread().getName(), submitSm.getSystemId(), sendId);
                        }


                    } else {
                        Thread.sleep(1000);
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                LOGGER.error("{}-{}- {} 处理短信下行异常", Thread.currentThread().getName(), sendId, mbl, e);
            }

        }

    }


}
