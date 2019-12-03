package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pdu.EnquireLink;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author huangzhuo
 * @date 2019/9/11 14:27
 */
public class EnquireLinkConsumer implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(EnquireLinkConsumer.class);

    @Override
    public void run() {
        List<String> isEnquireLink = new ArrayList<>();

        while (true) {

            try {
                Map<SessionKey, SmppSession> sessionMap = ClientInit.sessionMap;

                if (sessionMap != null && sessionMap.size() > 0) {
                    for (Map.Entry<SessionKey, SmppSession> entry : sessionMap.entrySet()) {
                        String systemId = entry.getKey().getSystemId();
                        //同一个systemid多个senderid只心跳一次
                        if (isEnquireLink.contains(systemId)) {
                            continue;
                        }
                        isEnquireLink.add(systemId);

                        SmppSession session0 = entry.getValue();
                        boolean bound = session0.isBound();
                        try {
                            LOGGER.info("-----------------------------systemId（{}），session状态为{}，开始心跳......", systemId, bound);
                            session0.enquireLink(new EnquireLink(), 10000);
                        } catch (Exception e) {
                            LOGGER.error("{}-{}心跳异常异常", Thread.currentThread().getName(), systemId, e);

                            session0.unbind(10000);
                            //如果心跳失败，则重新绑定一次，绑定失败 则移除该session
                            SmppSessionConfiguration smppSessionConfiguration = ClientInit.configMap.get(entry.getKey());
                            SmppSession client = ClientInit.createClient(smppSessionConfiguration);
                            if (client == null) {
                                LOGGER.error("{}-{}心跳异常异常且重新绑定失败", Thread.currentThread().getName(), systemId);
//								sessionMap.remove(entry.getKey());
                            }
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
            }

        }

    }


}
