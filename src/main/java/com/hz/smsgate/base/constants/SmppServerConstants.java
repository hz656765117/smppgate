package com.hz.smsgate.base.constants;

/**
 * @Auther: huangzhuo
 * @Date: 2019/11/4 21:00
 * @Description:
 */
public class SmppServerConstants {
	/**
	 * CM msgid的缓存key
	 */
	public static final String CM_MSGID_CACHE = "cmMsgIdCache";
	/**
	 * CM 短信下行缓存key
	 */
	public static final String CM_SUBMIT_SM = "cmSubmitSm";

	/**
	 *CM 状态报告缓存key
	 */
	public static final String CM_DELIVER_SM = "cmDeliverSm";


	/**
	 * WEB msgid的缓存key
	 */
	public static final String WEB_MSGID_CACHE = "webMsgIdCache";
	/**
	 * WEB 短信下行缓存key
	 */
	public static final String WEB_SUBMIT_SM = "webSubmitSm";

	/**
	 * WEB 长短信下行缓存key
	 */
	public static final String WEB_LONG_SUBMIT_SM = "webLongSubmitSm";

	/**
	 *WEB 状态报告缓存key
	 */
	public static final String WEB_DELIVER_SM = "webDeliverSm";


}
