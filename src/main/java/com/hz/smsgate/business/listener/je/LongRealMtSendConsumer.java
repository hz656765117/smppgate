package com.hz.smsgate.business.listener.je;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;


/**
 * 长短信发送
 *
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class LongRealMtSendConsumer implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(LongRealMtSendConsumer.class);

    @Autowired
    public RedisUtil redisUtil;

    public static LongRealMtSendConsumer longRealMtSendConsumer;

    @PostConstruct
    public void init() {
        longRealMtSendConsumer = this;
        longRealMtSendConsumer.redisUtil = this.redisUtil;
    }

    @Override
    public void run() {
        SubmitSm submitSm;


        while (true) {

            BlockingQueue<Object> realSendQueue = null;
            try {
                realSendQueue = BDBStoredMapFactoryImpl.INS.getQueue("realLongSubmitSmSend", "realLongSubmitSmSend");
            } catch (Exception e) {
                LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
            }

            try {
                if (realSendQueue != null) {
                    Object obj = realSendQueue.poll();
                    if (obj != null) {

                        submitSm = (SubmitSm) obj;

                        String[] tempMsgIds = submitSm.getTempMsgId().split("\\|");
                        //获取客户端session
                        SmppSession session0 = PduUtils.getSmppSession(submitSm);


                        SubmitSmResp submitResp = session0.submit(submitSm, 10000);


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
                LOGGER.error("{}-长短信分段下发异常", Thread.currentThread().getName(), e);
            }

        }

    }


}
