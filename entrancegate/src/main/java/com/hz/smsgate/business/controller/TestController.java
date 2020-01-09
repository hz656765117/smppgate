package com.hz.smsgate.business.controller;

import com.google.gson.Gson;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.utils.FileUtils;
import com.hz.smsgate.business.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

		return "test111";
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








	@RequestMapping("/login")
	public String welcome() {


		return "index";

	}


}
