package com.hz.smsgate.base.constants;



/**
 * @author huangzhuo
 * @date 2019/9/2 15:51
 */
public class StaticValue {


	public static String CLIENT_HOST = "";
	public static int CLIENT_PORT = 0000;
	public static String CLIENT_SYSTEMID = "";
	public static String CLIENT_PASSWORD = "";



	static {
		CLIENT_HOST = SystemGlobals.getValue("client.host") ;
		CLIENT_PORT = SystemGlobals.getIntValue("client.port") ;
		CLIENT_SYSTEMID = SystemGlobals.getValue("client.systemId") ;
		CLIENT_PASSWORD = SystemGlobals.getValue("client.password") ;

	}
}
