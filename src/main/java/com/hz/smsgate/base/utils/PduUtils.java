package com.hz.smsgate.base.utils;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pojo.Address;

/**
 * @Auther: huangzhuo
 * @Date: 2019/9/6 15:10
 * @Description:
 */
public class PduUtils {


	/**
	 * 通道555的短信去掉前面两个00
	 *
	 * @param sm
	 * @return
	 */
	public static SubmitSm removeZero(SubmitSm sm) {
		if (sm.getSourceAddress().getAddress().equals(StaticValue.CHANNL_REL.get("555")) || sm.getSourceAddress().getAddress().equals("555")) {
			Address destAddress = sm.getDestAddress();
			if (destAddress.getAddress().startsWith("00")) {
				String address = destAddress.getAddress().substring(2);
				destAddress.setAddress(address);
				sm.setDestAddress(destAddress);
			}
		}
		sm.calculateAndSetCommandLength();
		return sm;
	}

}
