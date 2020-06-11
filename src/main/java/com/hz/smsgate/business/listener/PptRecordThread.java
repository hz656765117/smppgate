package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.service.SmppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * redis状态报告记录线程
 *
 * @author huangzhuo
 * @date 2020/6/10 11:37
 */
@Component
public class PptRecordThread implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(PptRecordThread.class);

	@Autowired
	public RedisUtil redisUtil;

	public static PptRecordThread rptRecordThread;

	@Autowired
	public SmppService smppService;

	@PostConstruct
	public void init() {
		rptRecordThread = this;
		rptRecordThread.redisUtil = this.redisUtil;
		rptRecordThread.smppService = this.smppService;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}
		DeliverSm deliverSm;
		LOGGER.info("{}-记录状态报告线程开始工作......", Thread.currentThread().getName());
		Object obj;
		while (true) {
			try {
				//redis对象为空休眠一秒
				if (rptRecordThread.redisUtil == null) {
					Thread.sleep(1000);
					continue;
				}
				obj = rptRecordThread.redisUtil.rPop(SmppServerConstants.BACK_DELIVER_SM);
				//下行对象为空，从其他队列中获取，获取不到休眠半秒
				if (obj == null) {
					//通道没消息 则休眠1s
					Thread.sleep(1000);
					continue;
				}

				deliverSm = (DeliverSm) obj;
				//新增记录
				addRecord(deliverSm);


			} catch (Exception e) {
				LOGGER.error("{}- 记录状态报告异常", Thread.currentThread().getName(), e);
				try {
					Thread.sleep(10000);
				} catch (Exception e1) {

				}
			}


		}














	}

	private void addRecord(DeliverSm deliverSm) {
		try {
			boolean result = rptRecordThread.smppService.insertRptRecord(deliverSm);
			if (result) {
				return;
			}
			boolean flag = false;
			int i = 0;
			while (!flag && i <= 3) {
				flag = rptRecordThread.smppService.insertRptRecord(deliverSm);
				i++;
			}
		} catch (Exception e) {
			LOGGER.error("{}- 记录状态报告异常", Thread.currentThread().getName(), e);
		}

	}



}
