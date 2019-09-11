package com.hz.smsgate.base.constants;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author huangzhuo
 * @date 2019/9/2 15:51
 */
public class StaticValue {


	public static String CLIENT_HOST = "";
	public static int CLIENT_PORT = 0000;
	public static String CLIENT_SYSTEMID = "";
	public static String CLIENT_PASSWORD = "";

	public static int SERVER_PORT = 2776;


	public static int  ENQUIRE_LINK_TIME = 5000;

	public static String CHANNEL_1 = "555";
	public static String CHANNEL_2 = "882";



	public static Map<String ,String> CHANNL_REL = null;


	static {
		CLIENT_HOST = SystemGlobals.getValue("client.host") ;
		CLIENT_PORT = SystemGlobals.getIntValue("client.port") ;
		CLIENT_SYSTEMID = SystemGlobals.getValue("client.systemId") ;
		CLIENT_PASSWORD = SystemGlobals.getValue("client.password") ;


		SERVER_PORT = SystemGlobals.getIntValue("server.port" ,2776) ;

		ENQUIRE_LINK_TIME = SystemGlobals.getIntValue("enquire.link.time" ,20000) ;

		CHANNL_REL = new LinkedHashMap<>(1);
		CHANNL_REL.put(CHANNEL_2,"CMK");
		CHANNL_REL.put(CHANNEL_1,"BYTDNC");


	}
}
