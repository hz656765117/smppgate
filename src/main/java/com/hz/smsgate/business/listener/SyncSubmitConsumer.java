package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.emp.pojo.HttpSmsSend;
import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;


/**
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
public class SyncSubmitConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(SyncSubmitConsumer.class);


	@Override
	public void run() {
		WGParams wgParams;

		while (true) {

			BlockingQueue<Object> queue = null;
			try {
				queue = BDBStoredMapFactoryImpl.INS.getQueue("syncSubmit", "syncSubmit");
			} catch (Exception e) {
				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
			}

			try {
				if (queue != null) {
					Object obj = queue.poll();
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
