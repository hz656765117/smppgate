package com.hz.smsgate.business.controller;

import com.google.gson.Gson;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.config.SmppSessionConfiguration;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.utils.FileUtils;
import com.hz.smsgate.business.listener.ClientInit;
import com.hz.smsgate.business.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);
	@Autowired
	public RedisUtil redisUtil;

	@Value("${smsgate.type}")
	private String type;

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

		return type;
	}


	@CrossOrigin
	@RequestMapping("getResource")
	public String getResource() {
		LOGGER.info("获取资源列表");
		List<String> strings = FileUtils.readFileByLines(StaticValue.RESOURCE_HOME);
		Gson gson = new Gson();
		String listJson = gson.toJson(strings);
		return listJson;
	}

	@CrossOrigin
	@RequestMapping("getResourceById/{id}")
	public String getResourceById(@PathVariable(name = "id") String id) {
		LOGGER.info("根据id获取资源，id={}", id);
		List<String> strings = FileUtils.readFileByLines(StaticValue.RESOURCE_HOME);
		String s = strings.get(Integer.parseInt(id));
		return s;
	}


	@CrossOrigin
	@RequestMapping("delResourceById/{id}")
	public String delResourceById(@PathVariable(name = "id") String id) {
		LOGGER.info("根据id删除资源，id={}", id);
		List<String> strings = FileUtils.readFileByLines(StaticValue.RESOURCE_HOME);
		String curStr = strings.get(Integer.parseInt(id));
		strings.remove(Integer.parseInt(id));
		boolean b = FileUtils.writerTxt(strings, StaticValue.RESOURCE_HOME);

		String[] split = curStr.split("\\|");
		closeClientConnect(split[4]);


		return b + "";
	}


	@CrossOrigin
	@RequestMapping("addResource")
	public String addResource(@RequestParam(value = "resource", required = false) String resource) {
		LOGGER.info("新增资源，resource={}", resource);
		List<String> strings = FileUtils.readFileByLines(StaticValue.RESOURCE_HOME);
		strings.add(resource);
		boolean b = FileUtils.writerTxt(strings, StaticValue.RESOURCE_HOME);
		String[] split = resource.split("\\|");
//		openClientConnect(split[4]);
		return b + "";
	}

	@CrossOrigin
	@RequestMapping("updateResource")
	public String updateResource(@RequestParam(value = "id", required = false) int id, @RequestParam(value = "resource", required = false) String resource) {
		LOGGER.info("新增资源，id={}, resource={}", id, resource);
		List<String> strings = FileUtils.readFileByLines(StaticValue.RESOURCE_HOME);
		String oldResource = strings.get(id);
		strings.set(id, resource);
		boolean b = FileUtils.writerTxt(strings, StaticValue.RESOURCE_HOME);

		String[] split1 = oldResource.split("\\|");
		closeClientConnect(split1[4]);

		String[] split2 = resource.split("\\|");
//		openClientConnect(split2[4]);
		return b + "";
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
