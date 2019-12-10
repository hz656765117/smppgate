package com.hz.smsgate.base.utils;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.DeliveryReceipt;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.smsgate.business.listener.ClientInit;
import com.hz.smsgate.business.smpp.handler.DefaultSmppSessionHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppClient;
import com.hz.smsgate.business.smpp.impl.DefaultSmppServer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @Auther: huangzhuo
 * @Date: 2019/9/6 15:10
 * @Description:
 */
public class PduUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(PduUtils.class);

	/**
	 * 通道555，778,779的短信去掉前面两个00
	 *
	 * @param sm
	 * @return
	 */
	public static SubmitSm removeZero(SubmitSm sm) {
		String channel = sm.getSourceAddress().getAddress();
		SessionKey sessionKey = new SessionKey(sm.getSystemId(), channel);
		SessionKey sessionKey1 = ClientInit.CHANNL_REL.get(StaticValue.CHANNEL_1);
		boolean flag = sessionKey1 != null ? channel.equals(ClientInit.CHANNL_REL.get(StaticValue.CHANNEL_1).getSenderId()) : false;
		if (flag || channel.equals(StaticValue.CHANNEL_1) || ClientInit.CHANNEL_MK_LIST.contains(sessionKey)) {
			Address destAddress = sm.getDestAddress();
			if (destAddress.getAddress().startsWith("00")) {
				String address = destAddress.getAddress().substring(2);
				destAddress.setAddress(address);
				sm.setDestAddress(destAddress);
				sm.calculateAndSetCommandLength();
			}
		}
		return sm;
	}


	/**
	 * 获取区号
	 *
	 * @param mbl
	 * @return
	 */
	public static String getAreaCode(String mbl) {
		String areaCode = "";
		if (StringUtils.isBlank(mbl)) {
			return areaCode;
		}

		if (mbl.startsWith("00")) {
			areaCode = mbl.substring(2, 4);
		} else {
			areaCode = mbl.substring(0, 2);
		}

		return areaCode;
	}


	/**
	 * 根据通道获取用户名
	 *
	 * @param sendId 通道id
	 * @return
	 */
//	public static String getSystemIdBySendId(String sendId) {
//		SmppSessionConfiguration smppSessionConfiguration = ClientInit.configMap.get(sendId);
//		if (smppSessionConfiguration == null) {
//			String key = getKey(sendId);
//			smppSessionConfiguration = ClientInit.configMap.get(key);
//		}
//		if (smppSessionConfiguration == null) {
//			return null;
//		}
//		String systemId = smppSessionConfiguration.getSystemId();
//		return systemId;
//	}


	/**
	 * 短信内容GSM编码  cm运营商的需要编码
	 *
	 * @param sm 下行短信对象
	 * @return
	 */
	public static SubmitSm encodeGsm(SubmitSm sm) {
		String systemId = sm.getSystemId();
		//cm资源需要GSM格式编码
		if (StaticValue.SYSTEMID_CM_1.equals(systemId) || StaticValue.SYSTEMID_CM_2.equals(systemId) || StaticValue.SYSTEMID_ALEX.equals(systemId)) {
			onlyEncodeGsm(sm);
		}
		return sm;
	}

	/**
	 * 短信内容Gsm编码
	 *
	 * @param sm 下行短信对象
	 * @return
	 */
	public static SubmitSm onlyEncodeGsm(SubmitSm sm) {
		byte[] shortMessage = sm.getShortMessage();
		String content = new String(shortMessage);
		LOGGER.info("短短信的内容为{},下行号码为{}，通道为{}", content, sm.getDestAddress().getAddress(), sm.getSourceAddress().getAddress());
		try {
			byte[] textBytes = CharsetUtil.encode(content, CharsetUtil.CHARSET_GSM);
			sm.setShortMessage(textBytes);
		} catch (Exception e) {
			LOGGER.error("短信内容编码异常", e);
		}
		LOGGER.info("短短信编码后的内容为{},下行号码为{}，通道为{}", new String(content.getBytes()), sm.getDestAddress().getAddress(), sm.getSourceAddress().getAddress());

		sm.calculateAndSetCommandLength();
		return sm;
	}


	/**
	 * 重写下行对象
	 *
	 * @param sm
	 * @return
	 */
	public static SubmitSm rewriteSubmitSm(SubmitSm sm) {

		//通道替换
		sm = PduUtil.rewriteSmSourceAddress(sm);

		//短信下行内容编码
		sm = PduUtils.encodeGsm(sm);
		//通道555的短信去掉前面两个00
		sm = PduUtils.removeZero(sm);
		return sm;
	}


	/**
	 * 根据下行通道获取对应的客户端session  TODO  暂时不支持多个客户端获取
	 *
	 * @param sm
	 * @return
	 */
	public static SmppSession getSmppSession(SubmitSm sm) {
		String sendId = sm.getSourceAddress().getAddress();
		String systemId = sm.getSystemId();


		String key = getKey(systemId, sendId);

		SessionKey sessionKey = new SessionKey();
		sessionKey.setSystemId(systemId);
		sessionKey.setSenderId(sendId);


		SmppSession session0 = null;
		Map<SessionKey, SmppSession> sessionMap = ClientInit.sessionMap;
		if (sessionMap != null && sessionMap.size() > 0) {
			session0 = sessionMap.get(sessionKey);
			if (session0 == null) {
				sessionKey.setSenderId(key);
				session0 = sessionMap.get(sessionKey);
			}
		}

		if (session0 == null || !session0.isBound()) {
			try {
				sessionKey.setSenderId(sendId);
				SmppSessionConfiguration smppSessionConfiguration = ClientInit.configMap.get(sessionKey);
				if (smppSessionConfiguration == null) {
					sessionKey.setSenderId(key);
					smppSessionConfiguration = ClientInit.configMap.get(sessionKey);
				}

				session0 = ClientInit.createClient(smppSessionConfiguration);
				if (session0 != null) {
					return session0;
				}
			} catch (Exception e) {
				LOGGER.error("获取客户端连接异常", e);
			}
		}


		return session0;
	}

	public static String getKey(String systemId, String sendId) {
		Map<String, SessionKey> channlRel = ClientInit.CHANNL_REL;
		SessionKey sessionKey = new SessionKey(systemId, sendId);
		for (Map.Entry<String, SessionKey> entry : channlRel.entrySet()) {
			if (sessionKey.equals(entry.getValue())) {
				sendId = entry.getKey();
				break;
			}
		}
		return sendId;
	}


	//获取原通道
	public static String getRealChannel(String systemId, String gwChannel) {
		if (StringUtils.isBlank(gwChannel)) {
			return gwChannel;
		}
		SessionKey sessionKey = new SessionKey(systemId, gwChannel);
		for (Map.Entry<String, SessionKey> entry : ClientInit.CHANNL_REL.entrySet()) {
			if (sessionKey.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return gwChannel;
	}

	public static String getMsgId(DeliverSm deliverSm) {
		String str = new String(deliverSm.getShortMessage());
		DeliveryReceipt deliveryReceipt;
		String messageId = "";
		try {
			deliveryReceipt = DeliveryReceipt.parseShortMessage(str, DateTimeZone.UTC);
			messageId = deliveryReceipt.getMessageId();
		} catch (Exception e) {
			LOGGER.error("{}-处理短信状态报告内容解析异常", Thread.currentThread().getName(), e);
			return messageId;
		}
		return messageId;
	}


	public static SmppSession getServerSmppSession(DeliverSm deliverSm) {
		SmppSession smppSession = null;
		//根据通道获取session
		String channel = deliverSm.getDestAddress().getAddress();
		String systemId = deliverSm.getSystemId();


		if (DefaultSmppServer.smppSessionList == null || DefaultSmppServer.smppSessionList.size() < 1) {
			String msgId = getMsgId(deliverSm);
			LOGGER.error("{}-处理状态报告异常，未能获取到服务端连接(通道为：{}，systemId为：{},msgId为：({}))-------", Thread.currentThread().getName(), channel, systemId, msgId);
			return smppSession;
		}

		for (SmppSession session : DefaultSmppServer.smppSessionList) {
			if (session.getConfiguration().getSystemId().equals(systemId)) {
				smppSession = session;
				break;
			}
		}

		if (smppSession == null) {
			String msgId = getMsgId(deliverSm);
			LOGGER.error("{}-处理状态报告异常，未能匹配到服务端连接(通道为：{}，systemId为：{},msgId为：({}))-------", Thread.currentThread().getName(), channel, systemId, msgId);
			return smppSession;
		}
		return smppSession;
	}

}
