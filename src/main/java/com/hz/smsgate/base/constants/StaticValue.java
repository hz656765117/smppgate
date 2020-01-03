package com.hz.smsgate.base.constants;


import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.utils.FileUtils;
import com.hz.smsgate.business.listener.ClientInit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangzhuo
 * @date 2019/9/2 15:51
 */
public class StaticValue {

	/**
	 * 资源文件地址
	 */
	public static String RESOURCE_HOME = "";

	/**
	 * 服务端 端口
	 */
	public static int SERVER_PORT;

	/**
	 * 心跳频率
	 */
	public static int ENQUIRE_LINK_TIME;


	public static String CHANNEL_1 = "555";



	public static String SYSTEMID_CM_1 = "HaloPushingLtd";
	public static String SYSTEMID_CM_2 = "HaloMakreting";

	public static String SYSTEMID_ALEX = "Alex";






	//马来西亚区号
	public static String AREA_CODE_MALAYSIA = "60";

	//越南区号
	public static String AREA_CODE_VIETNAM = "84";

	//菲律宾
	public static String AREA_CODE_PHILIPPINES = "63";




	public static String WEB_GATE = "";



	static {

		RESOURCE_HOME = SystemGlobals.getValue("resource.home");

		SERVER_PORT = SystemGlobals.getIntValue("server.port", 2776);
		ENQUIRE_LINK_TIME = SystemGlobals.getIntValue("enquire.link.time", 20000);
		WEB_GATE = SystemGlobals.getValue("montnets.webgate");

	}


}
