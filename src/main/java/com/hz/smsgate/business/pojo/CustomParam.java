package com.hz.smsgate.business.pojo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Auther: huangzhuo
 * @Date: 2020/3/24 10:57
 * @Description:
 */
@Data
@Component
@ConfigurationProperties(prefix = "montnets")
public class CustomParam {
	/**
	 * 运营商类型
	 */
	private String operatorType;

	/**
	 * 短信网关ip
	 */
	private String smsgateIp;

	/**
	 * 短信网关port
	 */
	private String smsgatePort;


	private	boolean heartBeat;


	private long enquireTime;






}
