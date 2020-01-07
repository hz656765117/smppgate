package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;


/**
 * @author huangzhuo
 * @date 2019/12/13 10:58
 */
@Component
public class ClientBindConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(ClientBindConsumer.class);


	@Autowired
	public RedisUtil redisUtil;

	public static ClientBindConsumer clientBindConsumer;

	@PostConstruct
	public void init() {
		clientBindConsumer = this;
		clientBindConsumer.redisUtil = this.redisUtil;
	}


	@Override
	public void run() {
		try {
			Thread.sleep(30000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}

		SessionKey key;
		Object obj;
		LOGGER.info("{}-处理重新绑定的客户端线程（redis）开始工作......", Thread.currentThread().getName());

		while (true) {
			try {
				if (clientBindConsumer.redisUtil != null) {
					obj = clientBindConsumer.redisUtil.rPop(SmppServerConstants.WEB_BIND_AGAIN);
					if (obj != null) {
						key = (SessionKey) obj;
						LOGGER.info("{}-读取到需要重新绑定的客户端信息{}-{}", Thread.currentThread().getName(), key.getSystemId(), key.getSenderId());
						SmppSession session = ClientInit.sessionMap.get(key);
						try {
							session.unbind(10000);
						} catch (Exception e) {
							LOGGER.error("{}-{}心跳异常异常且解绑失败", Thread.currentThread().getName(), key.getSystemId());
						}

						//重新绑定
						SmppSessionConfiguration smppSessionConfiguration = ClientInit.configMap.get(key);
						SmppSession client = ClientInit.createClient(smppSessionConfiguration);
						if (client == null) {
							LOGGER.error("{}-{}心跳异常异常且重新绑定失败", Thread.currentThread().getName(), key.getSystemId());
							continue;
						}

						//更新整个运营商的所有senderid的session
						for (Map.Entry<SessionKey, SmppSession> entry : ClientInit.sessionMap.entrySet()) {
							if (entry.getKey().getSystemId().equals(key.getSystemId())) {
								ClientInit.sessionMap.put(entry.getKey(), client);
							}
						}
					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-处理重新绑定的客户端异常", Thread.currentThread().getName(), e);
				try {
					Thread.sleep(10000);
				} catch (Exception E) {

				}

			}

		}
	}


}
