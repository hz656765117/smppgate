package com.hz.smsgate.business.listener;

import com.cloudhopper.commons.util.windowing.OfferTimeoutException;
import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.exception.SmppTimeoutException;
import com.hz.smsgate.base.smpp.pdu.EnquireLink;
import com.hz.smsgate.base.smpp.pdu.EnquireLinkResp;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.MailUtil;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.base.utils.SpringContextUtil;
import com.hz.smsgate.business.pojo.CustomParam;
import com.hz.smsgate.business.service.SmppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author huangzhuo
 * @date 2019/9/11 14:27
 */
@Component
public class EnquireLinkConsumer implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(EnquireLinkConsumer.class);


    @Autowired
    public RedisUtil redisUtil;

    public static EnquireLinkConsumer enquireLinkConsumer;

    @PostConstruct
    public void init() {
        enquireLinkConsumer = this;
        enquireLinkConsumer.redisUtil = this.redisUtil;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
        }

        List<String> isEnquireLink = new ArrayList<>();
        Map<SessionKey, SmppSession> sessionMap;
        while (true) {

            try {
                sessionMap = ClientInit.sessionMap;

                if (sessionMap != null && sessionMap.size() > 0) {


                    Iterator<Map.Entry<SessionKey, SmppSession>> iterator = sessionMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<SessionKey, SmppSession> entry = iterator.next();
                        String systemId = entry.getKey().getSystemId();
                        //同一个systemid多个senderid只心跳一次
                        if (isEnquireLink.contains(systemId)) {
                            continue;
                        }

                        isEnquireLink.add(systemId);

                        SmppSession session0 = entry.getValue();
                        try {
                            LOGGER.info("-----------------------------systemId（{}）开始心跳......", systemId);
                            EnquireLinkResp enquireLinkResp = session0.enquireLink(new EnquireLink(), 10000);
                            if (enquireLinkResp.getCommandStatus() != 0) {
                                reBind(null, entry.getKey(), iterator, session0);
                                continue;
                            }

                        } catch (OfferTimeoutException te) {
                            LOGGER.error("-----------------------------systemId（{}）心跳超时不重新绑定---------------------", systemId, te);
                        } catch (Exception e) {
                            reBind(e, entry.getKey(), iterator, session0);
                        }
                        Thread.sleep(1000);
                    }


                    isEnquireLink.clear();
                    Thread.sleep(StaticValue.ENQUIRE_LINK_TIME);
                } else {
                    isEnquireLink.clear();
                    Thread.sleep(5000);
                }


            } catch (Exception e) {
                isEnquireLink.clear();
                LOGGER.error("{}-处理心跳异常", Thread.currentThread().getName(), e);
                try {
                    Thread.sleep(10000);
                } catch (Exception E) {

                }
            }

        }

    }


    public static void reBind(Exception e, SessionKey key, Iterator<Map.Entry<SessionKey, SmppSession>> iterator, SmppSession session) {
        if (e == null) {
            LOGGER.error("{}-{}心跳异常异常", Thread.currentThread().getName(), key.getSystemId());
        } else {
            LOGGER.error("{}-{}心跳异常异常", Thread.currentThread().getName(), key.getSystemId(), e);
        }


        boolean flag = SpringContextUtil.getBean(SmppService.class).needBindRecord(key.getSystemId());
        if (flag) {
            enquireLinkConsumer.redisUtil.lPush(SmppServerConstants.WEB_BIND_AGAIN, key);
        } else {
            LOGGER.error("-----近期连接次数太多，不去连接资源(systemid:{},sendId:{})------", key.getSystemId(), key.getSenderId());
            String name = session.getConfiguration().getHost() + "|" + session.getConfiguration().getSystemId();
            SmppSession session1 = ClientInit.existSystemId1s.get(name);
            if (session1 != null) {
                LOGGER.info("{}连接异常  告警", key.getSystemId());
                CustomParam customParam = SpringContextUtil.getBean(CustomParam.class);
                SpringContextUtil.getBean(MailUtil.class).sendSimpleMail(customParam.getMails(), "运营商状态异常", key.getSystemId() + "运营商连接异常，请管理员尽快关闭该运营商并反馈至运营商");
            }

            ClientInit.existSystemId1s.remove(name);

            iterator.remove();
        }


    }


}
