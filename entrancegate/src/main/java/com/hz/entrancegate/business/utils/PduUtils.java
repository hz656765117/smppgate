package com.hz.entrancegate.business.utils;

import com.hz.entrancegate.business.listener.ServerInit;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.entrancegate.business.pojo.SmppUserVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Auther: huangzhuo
 * @Date: 2019/9/6 15:10
 * @Description:
 */
public class PduUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(PduUtils.class);


	/**
	 * 获取区号
	 *
	 * @param mbl 手机号码
	 * @return 区号
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
	 * 获取号段
	 *
	 * @param mbl 手机号码
	 * @return 号段
	 */
	public static String getNumSeg(String mbl) {
		String numSeg = "";
		if (StringUtils.isBlank(mbl)) {
			return numSeg;
		}
		if (mbl.startsWith("00")) {
			numSeg = mbl.substring(0, 7);
		} else {
			numSeg = 00 + mbl.substring(0, 5);
		}
		return numSeg;
	}


	public static SmppUserVo getSmppUserByUserPwd(String smppUser, String smppPwd) {
		if (StringUtils.isBlank(smppUser) || StringUtils.isBlank(smppPwd)) {
			return null;
		}
		List<SmppUserVo> smppUser1 = ServerInit.HTTP_SMPP_USER;
		for (SmppUserVo smppUserVo : smppUser1) {
			if (smppUser.equals(smppUserVo.getSmppUser()) && smppPwd.equals(smppUserVo.getSmppPwd())) {
				return smppUserVo;
			}

		}
		return null;
	}


	/**
	 * 重写下行对象，将通道更改为正确的
	 *
	 * @param sm 下行对象
	 * @return 修改后的下行对象
	 */
	public static SubmitSm rewriteSmSourceAddress(SubmitSm sm) {
		Address sourceAddress = sm.getSourceAddress();
		int beforeLen = PduUtil.calculateByteSizeOfAddress(sourceAddress);
		SessionKey sessionKey = ServerInit.CHANNL_REL.get(sourceAddress.getAddress());
		if (sessionKey == null) {
			return sm;
		}
		String gwChannel = sessionKey.getSenderId();
		if (!StringUtils.isBlank(gwChannel)) {
			sourceAddress.setAddress(gwChannel);
		}

		int afterLen = PduUtil.calculateByteSizeOfAddress(sourceAddress);
		sm.setCommandLength(sm.getCommandLength() - beforeLen + afterLen);
		sm.setSourceAddress(sourceAddress);
		return sm;
	}

}
