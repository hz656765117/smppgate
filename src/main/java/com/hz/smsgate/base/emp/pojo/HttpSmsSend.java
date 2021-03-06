package com.hz.smsgate.base.emp.pojo;


import com.hz.smsgate.base.constants.StaticValue;

/**
 * 短信发送接口类
 *
 * @author Administrator
 */
public class HttpSmsSend {

	private SmsUtil smsutil = new SmsUtil();


	/**
	 * 网关发送接口
	 *
	 * @param params 网关动态参数
	 * @return 接收字符串
	 * @throws Exception
	 */
	public String createbatchMtRequest(WGParams params) throws Exception {

		//设置发送请求为号码群发，不是文件群发
		params.setCommand("MULTI_MT_REQUEST");

		//是否需要状态报告
		params.setRptflag("0");

		//调用网关接口
		return smsutil.execute(params, StaticValue.WEB_GATE);
	}





}
