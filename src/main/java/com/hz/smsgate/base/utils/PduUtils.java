package com.hz.smsgate.base.utils;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.business.listener.ClientInit;
import org.apache.commons.lang3.StringUtils;
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
	 * 通道555，778的短信去掉前面两个00
	 *
	 * @param sm
	 * @return
	 */
	public static SubmitSm removeZero(SubmitSm sm) {
		if (sm.getSourceAddress().getAddress().equals(StaticValue.CHANNL_REL.get(StaticValue.CHANNEL_1)) || sm.getSourceAddress().getAddress().equals(StaticValue.CHANNEL_1) || sm.getSourceAddress().getAddress().equals(StaticValue.CHANNEL_3)) {
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
	 * 短信内容GSM编码
	 *
	 * @param sm
	 * @return
	 */
	public static SubmitSm encodeGsm(SubmitSm sm) {

		if (sm.getSourceAddress().getAddress().equals(StaticValue.CHANNL_REL.get(StaticValue.CHANNEL_2)) || sm.getSourceAddress().getAddress().equals(StaticValue.CHANNEL_2)) {
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

		}

		return sm;
	}


	/**
	 * 重写下行对象
	 *
	 * @param sm
	 * @return
	 */
	public static SubmitSm rewriteSubmitSm(SubmitSm sm) {
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
		Map<String, String> channlRel = StaticValue.CHANNL_REL;

		for (Map.Entry<String, String> entry : channlRel.entrySet()) {
			if (sendId.equals(entry.getValue())) {
				sendId = entry.getKey();
				break;
			}
		}


		SmppSession session0 = null;
		Map<String, SmppSession> sessionMap = ClientInit.sessionMap;
		if (sessionMap != null && sessionMap.size() > 0) {
			session0 = sessionMap.get(sendId);
		}


		if (session0 == null) {
			try {
				session0 = ClientInit.clientBootstrapMap.get(sendId).bind(ClientInit.configMap.get(sendId), ClientInit.sessionHandlerMap.get(sendId));
				ClientInit.sessionMap.put(sendId, session0);
			} catch (Exception e) {
				LOGGER.error("获取客户端连接异常", e);
			}
		}
		return session0;
	}


}
