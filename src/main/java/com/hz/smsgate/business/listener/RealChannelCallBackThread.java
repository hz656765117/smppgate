package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.utils.HttpClientUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.pojo.SenderIdVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 真实channel回调线程
 *
 * @author huangzhuo
 * @date 2020/1/19 16:01
 */
@Component
public class RealChannelCallBackThread implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(RealChannelCallBackThread.class);

	@Autowired
	public RedisUtil redisUtil;

	public static RealChannelCallBackThread realChannelCallBackThread;


	@PostConstruct
	public void init() {
		realChannelCallBackThread = this;
		realChannelCallBackThread.redisUtil = this.redisUtil;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}
		SenderIdVo senderIdVo;
		LOGGER.info("{}-真实channel回调线程开始工作......", Thread.currentThread().getName());
		Object obj;
		while (true) {
			try {
				//redis对象为空休眠一秒
				if (realChannelCallBackThread.redisUtil == null) {
					Thread.sleep(1000);
					continue;
				}
				obj = realChannelCallBackThread.redisUtil.rPop(SmppServerConstants.REAL_CHANNEL_CACHE);
				//下行对象为空，从其他队列中获取，获取不到休眠半秒
				if (obj == null) {
					//通道没消息 则休眠1s
					Thread.sleep(1000);
					continue;
				}

				senderIdVo = (SenderIdVo) obj;

				sendToPt(senderIdVo);

			} catch (Exception e) {
				LOGGER.error("{}- 真实channel回调线程异常", Thread.currentThread().getName(), e);
				try {
					Thread.sleep(10000);
				} catch (Exception e1) {

				}
			}


		}

	}


	private void sendToPt(SenderIdVo senderIdVo) {
		if (senderIdVo == null) {
			LOGGER.error("{}- 真实channel回调异常，senderIdVo对象为空", Thread.currentThread().getName());
			return;
		}
		try {
			String url = StaticValue.HTTP_WEB + "/rvsmpp.hts";

			HashMap<String, String> headerMap = new HashMap<>(1);
			headerMap.put("Connection", "close");

			Map<String, String> param = new LinkedHashMap<>(4);
			param.put("systemid", senderIdVo.getSystemId());
			param.put("msgid", senderIdVo.getMsgId());
			param.put("spgate", senderIdVo.getChannel());
			param.put("spno", senderIdVo.getRealChannel());
			HttpClientUtils.doPost(url, param, headerMap);
		} catch (Exception e) {
			LOGGER.error("{}- 真实channel回调异常", Thread.currentThread().getName(), e);
		}
	}

}
