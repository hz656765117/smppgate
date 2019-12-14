package com.hz.smsgate.business.listener.redis;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.DeliveryReceipt;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.ClientInit;
import com.hz.smsgate.business.pojo.MsgVo;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;


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
		try {
			Thread.sleep(30000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}

		DeliverSm deliverSm;
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


	/**
	 * 重写状态报告对象
	 *
	 * @param deliverSm 状态报告
	 * @return 状态报告对象
	 */
	private DeliverSm reWriteDeliverSm(DeliverSm deliverSm) {
		//替换真实通道
		Address destAddress = deliverSm.getDestAddress();
		String realChannel = PduUtils.getRealChannel(deliverSm.getSystemId(), destAddress.getAddress());
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


	private void sendDeliverSm(DeliverSm deliverSm) {
		Map<String, String> removeMap = new LinkedHashMap<>();


		String str = new String(deliverSm.getShortMessage());
		DeliveryReceipt deliveryReceipt;
		String messageId;
		try {
			deliveryReceipt = DeliveryReceipt.parseShortMessage(str, DateTimeZone.UTC);
			messageId = deliveryReceipt.getMessageId();
		} catch (Exception e) {
			LOGGER.error("{}-处理短信状态报告内容解析异常", Thread.currentThread().getName(), e);
			return;
		}


		Object obj = rptRedisConsumer.redisUtil.hmGet(SmppServerConstants.WEB_MSGID_CACHE, messageId);
		if (obj != null) {
			try {
				MsgVo msgVo = (MsgVo) obj;
				String preMsgId = msgVo.getMsgId();

				SmppSession smppSession = PduUtils.getServerSmppSession(deliverSm);
				if (smppSession == null) {
					return;
				}


				SessionKey sessionKey = new SessionKey(deliverSm.getSystemId(), msgVo.getSenderId());
				//这个通道的运营商会返回两个状态报告 忽略掉accepted  只处理Delivered
				if (ClientInit.CHANNEL_MK_LIST.contains(sessionKey)) {
					String mbl = deliverSm.getSourceAddress().getAddress();
					String areaCode = PduUtils.getAreaCode(mbl);
					//马来西亚和菲律宾 只有accepted  || StaticValue.AREA_CODE_VIETNAM.equals(areaCode)
					if (StaticValue.AREA_CODE_MALAYSIA.equals(areaCode)  || StaticValue.AREA_CODE_PHILIPPINES.equals(areaCode)) {
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
				LOGGER.info("{}-{}状态报告响应msgid为{}，缓存中key为{}，value为{}", deliverSm.getSystemId(), deliverSm.getDestAddress().getAddress(), deliverSm.getSourceAddress().getAddress(), messageId, messageId, preMsgId);
				String[] tempMsgIds;
				if (preMsgId.contains("|")) {
					tempMsgIds = preMsgId.split("\\|");
				} else {
					tempMsgIds = new String[1];
					tempMsgIds[0] = preMsgId;
				}
				removeMap.put(messageId, preMsgId);

				for (String curMsgId : tempMsgIds) {
					String[] split = curMsgId.split("-");
					//替换messageId
					deliveryReceipt.setMessageId(split[0]);
					byte[] bytes = deliveryReceipt.toShortMessage().getBytes();
					deliverSm.setShortMessage(bytes);
					deliverSm.removeSequenceNumber();
					Address destAddress = deliverSm.getDestAddress();
					destAddress.setAddress(msgVo.getSenderId());
					deliverSm.setDestAddress(destAddress);
					deliverSm.calculateAndSetCommandLength();

					//重写状态报告
					deliverSm = reWriteDeliverSm(deliverSm);

					smppSession.sendRequestPdu(deliverSm, 10000, true);
				}


			} catch (Exception e) {
				rptRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_DELIVER_SM,deliverSm);
				LOGGER.error("{}-  systemid为{},{}-{}，msgid={}  ，处理长短信状态报告转发异常", Thread.currentThread().getName(), deliverSm.getSystemId(), deliverSm.getSystemId(), deliverSm.getDestAddress().getAddress(), deliverSm.getSourceAddress().getAddress(), messageId, e);
				return;
			}

			if (removeMap.size() > 0) {
				for (Map.Entry<String, String> entry : removeMap.entrySet()) {
					rptRedisConsumer.redisUtil.hmRemove(SmppServerConstants.WEB_MSGID_CACHE, entry.getKey());
				}
			}

		} else {
			LOGGER.error("{}- systemid为{},{}-{}，msgid={}，未能匹配到对应的下行记录", Thread.currentThread().getName(), deliverSm.getSystemId(), deliverSm.getDestAddress().getAddress(), deliverSm.getSourceAddress().getAddress(), messageId);
		}


	}


}
