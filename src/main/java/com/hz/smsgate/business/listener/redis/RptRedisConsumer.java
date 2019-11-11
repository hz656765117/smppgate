package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.DeliveryReceipt;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


/**
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class RptRedisConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(RptRedisConsumer.class);


	@Autowired
	public RedisUtil redisUtil;

	public static RptRedisConsumer rptRedisConsumer;

	@PostConstruct
	public void init() {
		rptRedisConsumer = this;
		rptRedisConsumer.redisUtil = this.redisUtil;
	}

	//key为 msgid + 后缀     value 为 运营商的真实msgid
//	public static final Map<String, String> CACHE_MAP = new LinkedHashMap<>();

	@Override
	public void run() {
		DeliverSm deliverSm;

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			LOGGER.error("{}-处理短信状态报告转发线程（redis）启动异常", Thread.currentThread().getName(), e);
		}
		LOGGER.info("{}-处理短信状态报告转发线程（redis）开始工作......", Thread.currentThread().getName());

		while (true) {
			try {
				if (rptRedisConsumer.redisUtil != null) {
					Object obj = rptRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_DELIVER_SM);
					if (obj != null) {
						deliverSm = (DeliverSm) obj;
						LOGGER.info("{}-读取到状态报告信息{}", Thread.currentThread().getName(), deliverSm.toString());
						sendDeliverSm(deliverSm);
					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-处理短信状态报告转发异常", Thread.currentThread().getName(), e);
			}

		}
	}

	//获取原通道
	public String getRealChannel(String gwChannel) {
		if (StringUtils.isBlank(gwChannel)) {
			return gwChannel;
		}
		for (Map.Entry<String, String> entry : StaticValue.CHANNL_REL.entrySet()) {
			if (gwChannel.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return gwChannel;
	}

	/**
	 * @param deliverSm
	 * @return
	 */
	public DeliverSm reWriteDeliverSm(DeliverSm deliverSm) {
		//替换真实通道
		Address destAddress = deliverSm.getDestAddress();
		String realChannel = getRealChannel(destAddress.getAddress());
		destAddress.setAddress(realChannel);
		deliverSm.setDestAddress(destAddress);


		//补齐号码
		Address sourceAddress = deliverSm.getSourceAddress();
		String address1 = sourceAddress.getAddress();
		if (!address1.startsWith("0")) {
			address1 = "00" + address1;
			sourceAddress.setAddress(address1);
			deliverSm.setSourceAddress(sourceAddress);

		}


		deliverSm.calculateAndSetCommandLength();
		return deliverSm;
	}


	public void sendDeliverSm(DeliverSm deliverSm) {
		Map<String, String> removeMap = new LinkedHashMap<>();

		SmppSession smppSession = PduUtils.getServerSmppSession(deliverSm);
		if (smppSession == null) {
			return;
		}

		deliverSm = reWriteDeliverSm(deliverSm);


		String str = new String(deliverSm.getShortMessage());
		DeliveryReceipt deliveryReceipt = null;
		String messageId = "";
		try {
			deliveryReceipt = DeliveryReceipt.parseShortMessage(str, DateTimeZone.UTC);
			messageId = deliveryReceipt.getMessageId();
		} catch (Exception e) {
			LOGGER.error("{}-处理短信状态报告内容解析异常", Thread.currentThread().getName(), e);
			return;
		}

		//这个通道的运营商会返回两个状态报告 忽略掉accepted  只处理Delivered
		if (deliverSm.getDestAddress().getAddress().equals(StaticValue.CHANNEL_MK_1)) {
			String mbl = deliverSm.getSourceAddress().getAddress();
			String areaCode = PduUtils.getAreaCode(mbl);
			//马来西亚和越南 只有accepted
			if (StaticValue.AREA_CODE_MALAYSIA.equals(areaCode) || StaticValue.AREA_CODE_VIETNAM.equals(areaCode)) {
				if (deliveryReceipt.getState() == SmppConstants.STATE_ACCEPTED) {
					deliveryReceipt.setState(SmppConstants.STATE_DELIVERED);
				}
			} else {
				if (deliveryReceipt.getState() == SmppConstants.STATE_ACCEPTED) {
					LOGGER.info("渠道为：{}的状态报告，状态为：{}的丢弃,状态报告信息为：{}", deliverSm.getDestAddress().getAddress(), deliveryReceipt.getState(), deliveryReceipt.toString());
					return;
				}
			}

		}

		//key为 msgid + 后缀     value 为 运营商的真实msgid
		Map<String, String> msgidCache = (Map<String, String>) rptRedisConsumer.redisUtil.hmGetAll(SmppServerConstants.WEB_MSGID_CACHE);

		for (Map.Entry<String, String> entry : msgidCache.entrySet()) {
			//生成的Msgid
			String msgId = entry.getKey();

			//资源处下行响应时更新的MsgId
			String address = entry.getValue();

			try {

				if (address.equals(messageId)) {
					LOGGER.info("状态报告响应msgid为{}，缓存中key为{}，value为{}", messageId, entry.getKey(), entry.getValue());

					String[] split = msgId.split("-");
					if (split != null && split.length > 3) {
						//替换sequenceNumber
						deliverSm.setSequenceNumber(Integer.valueOf(split[3]));
					}

					//替换messageId
					deliveryReceipt.setMessageId(split[0]);
					byte[] bytes = deliveryReceipt.toShortMessage().getBytes();
					deliverSm.setShortMessage(bytes);
					deliverSm.calculateAndSetCommandLength();

					removeMap.put(entry.getKey(), entry.getValue());
					smppSession.sendRequestPdu(deliverSm, 10000, true);
				}
			} catch (Exception e) {
				LOGGER.error("{}-处理长短信状态报告转发异常", Thread.currentThread().getName(), e);
			}

		}


		if (removeMap != null && removeMap.size() > 0) {
			for (Map.Entry<String, String> entry : removeMap.entrySet()) {
				rptRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, entry.getKey());
			}
		} else {
			try {

				byte[] bytes = deliveryReceipt.toShortMessage().getBytes();
				deliverSm.setShortMessage(bytes);
				deliverSm.calculateAndSetCommandLength();

				smppSession.sendRequestPdu(deliverSm, 10000, true);
			} catch (Exception e) {
				LOGGER.error("{}-处理短短信状态报告转发异常", Thread.currentThread().getName(), e);
			}
		}


	}


}
