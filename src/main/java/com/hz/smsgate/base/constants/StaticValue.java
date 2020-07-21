package com.hz.smsgate.base.constants;


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


	public static String SYSTEMID_MK_4 = "HP04";
	public static String SYSTEMID_MK_6 = "HP06";

	public static String SYSTEMID_CM_1 = "HaloPushingLtd";
	public static String SYSTEMID_CM_2 = "HaloMakreting";
	public static String SYSTEMID_CM_3 = "HaloPUSH2";

	public static String SYSTEMID_ALEX = "Alex";

	public static String SYSTEMID_SA = "SA015a";

	public static String SYSTEMID_JATIS = "testotp";

	public static List<String> CHANNEL_JATIS_LIST = new ArrayList<>();

	public static String YN_TELKOMSEL = "0062811,0062812,0062813,0062852,0062853,0062821,0062822,0062823,0062854,0062851L";




	//马来西亚区号
	public static String AREA_CODE_MALAYSIA = "60";

	//越南区号
	public static String AREA_CODE_VIETNAM = "84";

	//菲律宾
	public static String AREA_CODE_PHILIPPINES = "63";



	public static String WEB_GATE = "";

	public static String HTTP_WEB = "";

	public static String TYPE = "";


	static {

		CHANNEL_JATIS_LIST.add("testotp");
		CHANNEL_JATIS_LIST.add("iitsmpp0");
		CHANNEL_JATIS_LIST.add("iitsmpp1");
		CHANNEL_JATIS_LIST.add("iitsmpp2");

		TYPE = SystemGlobals.getValue("montnets.type", "0");

		RESOURCE_HOME = SystemGlobals.getValue("resource.home");

		SP_RESOURCE_HOME = SystemGlobals.getValue("sp.resource.home");

		SERVER_PORT = SystemGlobals.getIntValue("server.port", 2776);

		ENQUIRE_LINK_TIME = SystemGlobals.getIntValue("enquire.link.time", 20000);
		WEB_GATE = SystemGlobals.getValue("montnets.webgate");


		HTTP_WEB= SystemGlobals.getValue("montnets.webgate1");
	}


	public static Map<String, String> initChannelRel() {
		Map<String, String> list = new LinkedHashMap<>();

		return list;
	}


}
