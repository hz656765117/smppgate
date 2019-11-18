package com.hz.smsgate.base.constants;


import com.hz.smsgate.base.emp.pojo.WGParams;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.utils.FileUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangzhuo
 * @date 2019/9/2 15:51
 */
public class StaticValue {

	//资源文件地址
	public static String RESOURCE_HOME = "";

	//SP账号资源文件地址
	public static String SP_RESOURCE_HOME = "";

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
	public static String CHANNEL_CM_6 = "887";
	public static String CHANNEL_CM_7 = "8881";
	public static String CHANNEL_CM_8 = "8882";
	public static String CHANNEL_CM_9 = "8883";


	public static String SYSTEMID_CM = "HaloPushingLtd";


	public static String CHANNEL_MK_1 = "778";
	public static String CHANNEL_MK_2 = "779";

	public static String CHANNEL_YN_1 = "668";


	public static List<String> CHANNEL_MK_LIST = new ArrayList<>();


	//马来西亚区号
	public static String AREA_CODE_MALAYSIA = "60";

	//越南区号
	public static String AREA_CODE_VIETNAM = "84";

	public static Map<String, SessionKey> CHANNL_REL = null;

	public static Map<String, WGParams> CHANNL_SP_REL = null;


	public static String WEB_GATE = "";

	public static String TYPE = "";


	static {

		TYPE = SystemGlobals.getValue("montnets.type", "0");

		RESOURCE_HOME = SystemGlobals.getValue("resource.home");

		SP_RESOURCE_HOME = SystemGlobals.getValue("sp.resource.home");

		SERVER_PORT = SystemGlobals.getIntValue("server.port", 2776);

		ENQUIRE_LINK_TIME = SystemGlobals.getIntValue("enquire.link.time", 20000);

		CHANNL_REL = new LinkedHashMap<>();

		CHANNL_REL.put(CHANNEL_CM_1, new SessionKey("HaloPushingLtd", "CMK"));
		CHANNL_REL.put(CHANNEL_CM_2, new SessionKey("HaloPushingLtd", "HAALOO"));
		CHANNL_REL.put(CHANNEL_CM_3, new SessionKey("HaloPushingLtd", "HALLOO"));
		CHANNL_REL.put(CHANNEL_CM_4, new SessionKey("HaloPushingLtd", "FINLNX"));
		CHANNL_REL.put(CHANNEL_CM_5, new SessionKey("HaloPushingLtd", "INLINX"));
		CHANNL_REL.put(CHANNEL_CM_6, new SessionKey("HaloPushingLtd", "CASHTP"));
		CHANNL_REL.put(CHANNEL_CM_7, new SessionKey("HaloPushingLtd", "MONEYB"));

		CHANNL_REL.put(CHANNEL_CM_9, new SessionKey("HaloMakreting", "HAALOO"));


		CHANNL_REL.put(CHANNEL_1, new SessionKey("INFIPRO", "BYTDNC"));

		CHANNL_REL.put(CHANNEL_MK_2, new SessionKey("HP01", "etracker"));

		CHANNL_REL.put(CHANNEL_YN_1, new SessionKey("Alex", "Alex"));


		CHANNEL_MK_LIST.add(CHANNEL_MK_1);
		CHANNEL_MK_LIST.add(CHANNEL_MK_2);
		CHANNEL_MK_LIST.add(CHANNL_REL.get(CHANNEL_MK_2).getSenderId());

		CHANNL_SP_REL = new LinkedHashMap<>();


		WEB_GATE = SystemGlobals.getValue("montnets.webgate");

		CHANNL_SP_REL = FileUtils.getSpConfigs(StaticValue.SP_RESOURCE_HOME);
	}


	public static Map<String, String> initChannelRel() {
		Map<String, String> list = new LinkedHashMap<>();

		return list;
	}


}
