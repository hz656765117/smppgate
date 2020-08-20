package com.hz.smsgate.business.controller;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.google.gson.Gson;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.FileUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.business.listener.ClientInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class TestController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);
	@Autowired
	public RedisUtil redisUtil;

	@RequestMapping(value = "/push")
	public void demoTest() {
		redisUtil.set("1", "value22222");
		redisUtil.lPush("duilie", "111");
		redisUtil.lPush("duilie", "222");
		redisUtil.lPush("duilie", "333");
	}


	@RequestMapping(value = "/pop")
	public void pop() {
		Object duilie = redisUtil.rPop("duilie");
		Object duilie1 = redisUtil.rPop("duilie");
		Object duilie2 = redisUtil.rPop("duilie");


	}


	@RequestMapping("test1")
	public String getSomething1() throws Exception {
		LOGGER.debug("aaaaaaaaaaadebugdebugdebugtest1111123434");
		LOGGER.info("aaaaaaaaaaaaaaaaaaainfoinfoinfotest1111123434");
		LOGGER.error("aaaaaaaaaaaaaaaaerrorerrorerrortest1111123434");
		LOGGER.warn("aaaaaaaaaaaaaawarnwarnwarntest1warnwarn111123434");
		Map<SessionKey, SmppSession> sessionMap = ClientInit.sessionMap;
		SmppSession session0 = null;



		String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
		byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM);
		SubmitSm submit0 = new SubmitSm();

		submit0.setSourceAddress(new Address((byte) 0x03, (byte) 0x00, "40404"));
		submit0.setDestAddress(new Address((byte) 0x01, (byte) 0x01, "44555519205"));
		try {
			submit0.setShortMessage(textBytes);
			SubmitSmResp submitResp = session0.submit(submit0, 10000);
		} catch (Exception e) {

		}
		session0.unbind(10000);
		session0.destroy();


		return "test111";
	}


	@RequestMapping("loglevel")
	public String getSomething2() throws Exception {
		LOGGER.debug("aaaaaaaaaaadebugdebugdebugtest1111123434");
		LOGGER.info("aaaaaaaaaaaaaaaaaaainfoinfoinfotest1111123434");
		LOGGER.error("aaaaaaaaaaaaaaaaerrorerrorerrortest1111123434");
		LOGGER.warn("aaaaaaaaaaaaaawarnwarnwarntest1warnwarn111123434");
		return "test111";
	}











	@CrossOrigin
	@RequestMapping("flushClientConnect")
	public String flushClientConnect() {
		LOGGER.info("刷新客户端连接");
		//如果心跳失败，则重新绑定一次，绑定失败 则移除该session
		for (Map.Entry<SessionKey, SmppSession> entry : ClientInit.sessionMap.entrySet()) {
			entry.getValue().close();
			ClientInit.sessionMap.remove(entry.getKey());
		}

		for (Map.Entry<SessionKey, SmppSessionConfiguration> entry : ClientInit.configMap.entrySet()) {
			ClientInit.createClient(entry.getValue());
		}

		return "complet";
	}


	public void closeClientConnect(String key) {
		LOGGER.info("关闭客户端连接，key={}", key);
		SmppSession smppSession = ClientInit.sessionMap.get(key);
		if (smppSession != null) {
			smppSession.close();
		}

		ClientInit.sessionMap.remove(key);
	}

//	public void openClientConnect(String key) {
//		LOGGER.info("打开客户端连接，key={}", key);
//		ClientInit.initConfigs();
//		SmppSessionConfiguration config = ClientInit.configMap.get(key);
//		ClientInit.createClient(config);
//
//	}


	@RequestMapping("/login")
	public String welcome() {


		return "index";

	}


}
