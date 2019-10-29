package com.hz.smsgate.base.constants;


import com.hz.smsgate.base.emp.pojo.WGParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author huangzhuo
 * @date 2019/9/2 15:51
 */
public class StaticValue {

	//资源文件地址
	public static String RESOURCE_HOME = "";

	//服务端 端口
	public static int SERVER_PORT = 2776;

	//心跳频率
	public static int ENQUIRE_LINK_TIME = 20000;


	public static String CHANNEL_1 = "555";
	public static String CHANNEL_CM_1 = "882";
	public static String CHANNEL_CM_2 = "883";
	public static String CHANNEL_CM_3 = "884";
	public static String CHANNEL_CM_4 = "885";
	public static String CHANNEL_CM_5 = "886";


	public static String SYSTEMID_CM = "HaloPushingLtd";


	public static String CHANNEL_3 = "778";

	//马来西亚区号
	public static String AREA_CODE_MALAYSIA = "60";

	//越南区号
	public static String AREA_CODE_VIETNAM = "84";

	public static Map<String, String> CHANNL_REL = null;

	public static Map<String, WGParams> CHANNL_SP_REL = null;


	public static String WEB_GATE = "";

	public static String SP_ID = "";

	public static String SP_PWD = "";

	public static String TYPE = "";


	static {

		TYPE = SystemGlobals.getValue("montnets.type","0");

		RESOURCE_HOME = SystemGlobals.getValue("resource.home");

		SERVER_PORT = SystemGlobals.getIntValue("server.port", 2776);

		ENQUIRE_LINK_TIME = SystemGlobals.getIntValue("enquire.link.time", 20000);

		CHANNL_REL = new LinkedHashMap<>();
		CHANNL_REL.put(CHANNEL_CM_1, "CMK");
		CHANNL_REL.put(CHANNEL_CM_2, "HAALOO");
		CHANNL_REL.put(CHANNEL_CM_3, "HALLOO");
		CHANNL_REL.put(CHANNEL_CM_4, "FINLNX");
		CHANNL_REL.put(CHANNEL_CM_5, "INLINX");

		CHANNL_REL.put(CHANNEL_1, "BYTDNC");

		CHANNL_SP_REL = new LinkedHashMap<>();

		WEB_GATE = SystemGlobals.getValue("montnets.webgate");
		SP_ID = SystemGlobals.getValue("montnets.spid");
		SP_PWD = SystemGlobals.getValue("montnets.sppwd");


	}
}
