package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.emp.pojo.HttpSmsSend;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.redis.LongMtMergeRedisConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;


/**
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class SyncSubmitConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(SyncSubmitConsumer.class);



	@Autowired
	public RedisUtil redisUtil;

	public static SyncSubmitConsumer syncSubmitConsumer;

	@PostConstruct
	public void init() {
		syncSubmitConsumer = this;
		syncSubmitConsumer.redisUtil = this.redisUtil;
	}

	@Override
	public void run() {
		WGParams wgParams;

		while (true) {



			try {
				if (syncSubmitConsumer.redisUtil != null) {
					Object obj = syncSubmitConsumer.redisUtil.rPop(SmppServerConstants.SYNC_SUBMIT);
					if (obj != null) {
						wgParams = (WGParams) obj;
						LOGGER.info("{}-读取到同步短信信息{}", Thread.currentThread().getName(), wgParams.toString());

						//补齐号码
						String mbl = wgParams.getDas();
						if (!mbl.startsWith("0")) {
							mbl = "00" + mbl;
						}
						wgParams.setDas(mbl);
						new HttpSmsSend().createbatchMtRequest(wgParams);

					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-处理同步短信信息异常", Thread.currentThread().getName(), e);
			}

		}
	}


}
