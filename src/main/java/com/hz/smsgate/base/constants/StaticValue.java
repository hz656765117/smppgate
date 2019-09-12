package com.hz.smsgate.base.constants;


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
	public static int  ENQUIRE_LINK_TIME = 20000;


	public static String CHANNEL_1 = "555";
	public static String CHANNEL_2 = "882";



	public static Map<String ,String> CHANNL_REL = null;


	static {

		RESOURCE_HOME = SystemGlobals.getValue("resource.home");

		SERVER_PORT = SystemGlobals.getIntValue("server.port" ,2776) ;

		ENQUIRE_LINK_TIME = SystemGlobals.getIntValue("enquire.link.time" ,20000) ;

		CHANNL_REL = new LinkedHashMap<>(1);
		CHANNL_REL.put(CHANNEL_2,"CMK");
		CHANNL_REL.put(CHANNEL_1,"BYTDNC");


	}
}
