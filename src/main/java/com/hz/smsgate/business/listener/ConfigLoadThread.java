package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pdu.EnquireLink;
import com.hz.smsgate.base.smpp.pdu.EnquireLinkResp;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.business.listener.redis.LongRealMtSendRedisConsumer;
import com.hz.smsgate.business.pojo.OperatorVo;
import com.hz.smsgate.business.service.SmppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 定时加载数据库中的配置到内存中
 *
 * @author huangzhuo
 * @date 2019/9/11 14:27
 */
@Component
public class ConfigLoadThread implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfigLoadThread.class);

	@Autowired
	ClientInit clientInit;

	public static ConfigLoadThread configLoadThread;

	@PostConstruct
	public void init() {
		configLoadThread = this;
		configLoadThread.clientInit = this.clientInit;
	}

	@Override
	public void run() {

		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}

		while (true) {

			try {
				Thread.sleep(30000);

				//初始化通道
				configLoadThread.clientInit.initChannels();
				//初始化优先级
				configLoadThread.clientInit.initYxj();
				//初始化mk集合
				configLoadThread.clientInit.initMkList();
				//初始化sp账号
				configLoadThread.clientInit.initSpList();


				configLoadThread.clientInit.initClientConfigs();



			} catch (Exception e) {
				LOGGER.error("{}-处理定时加载数据库中的配置到内存中异常", Thread.currentThread().getName(), e);
			}

		}

	}


}
