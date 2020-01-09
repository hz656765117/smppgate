package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.constants.SystemGlobals;
import com.hz.smsgate.base.smpp.config.SmppServerConfiguration;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.utils.PropertiesLoader;
import com.hz.smsgate.base.utils.SmppUtils;
import com.hz.smsgate.business.pojo.OperatorVo;
import com.hz.smsgate.business.pojo.SmppUserVo;
import com.hz.smsgate.business.service.SmppService;
import com.hz.smsgate.business.smpp.handler.DefaultSmppServerHandler;
import com.hz.smsgate.business.smpp.impl.DefaultSmppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: huangzhuo
 * @Date: 2019/8/16 15:26
 * @Description:
 */


@Configuration
public class ServerInit {
	private static final Logger logger = LoggerFactory.getLogger(ServerInit.class);

	@Autowired
	private SmppService smppService;


	public static Map<String, SessionKey> CHANNL_REL = new LinkedHashMap<>();

	/**
	 * opt通道
	 */
	public static List<SessionKey> CHANNEL_OPT_LIST = new ArrayList<>();
	/**
	 * 营销通道
	 */
	public static List<SessionKey> CHANNEL_YX_LIST = new ArrayList<>();
	/**
	 * 通知通道
	 */
	public static List<SessionKey> CHANNEL_TZ_LIST = new ArrayList<>();


	/**
	 * http路由账号
	 */
	public static List<SmppUserVo> HTTP_SMPP_USER = new LinkedList<>();

	@PostConstruct
	public void postConstruct() throws Exception {

		initSystemGlobals();

		//初始化通道
		initChannels();
		//初始化优先级
		initYxj();
		//初始化http路由账号
		initHttpSmppUser();


		int serverPort = StaticValue.SERVER_PORT;

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		ScheduledThreadPoolExecutor monitorExecutor = SmppUtils.getThreadPool("SmppServerSessionWindowMonitorPool");

		// create a server configuration
		SmppServerConfiguration configuration = SmppUtils.getServerConfig(serverPort);

		// create a server, start it up
		DefaultSmppServer smppServer = new DefaultSmppServer(configuration, new DefaultSmppServerHandler(), executor, monitorExecutor);

		logger.info("Starting SMPP server...  port is {}", serverPort);
		smppServer.start();
		logger.info("SMPP server started");


	}


	public void initHttpSmppUser() {
		HTTP_SMPP_USER.clear();
		HTTP_SMPP_USER = smppService.getHttpAllSmppUser();
	}

	public void initYxj() {
		List<OperatorVo> allOperator = smppService.getAllOperator();
		CHANNEL_OPT_LIST.clear();
		CHANNEL_TZ_LIST.clear();
		CHANNEL_YX_LIST.clear();
		for (OperatorVo operatorVo : allOperator) {
			SessionKey sessionKey = new SessionKey(operatorVo.getSystemid(), operatorVo.getChannel());
			if (operatorVo.getType() != null && 0 == operatorVo.getType()) {
				CHANNEL_OPT_LIST.add(sessionKey);
				CHANNEL_OPT_LIST.add(CHANNL_REL.get(operatorVo.getChannel()));
			} else if (operatorVo.getType() != null && 1 == operatorVo.getType()) {
				CHANNEL_TZ_LIST.add(sessionKey);
				CHANNEL_TZ_LIST.add(CHANNL_REL.get(operatorVo.getChannel()));
			} else {
				CHANNEL_YX_LIST.add(sessionKey);
				CHANNEL_YX_LIST.add(CHANNL_REL.get(operatorVo.getChannel()));
			}
		}
	}


	public void initChannels() {
		Map<String, SessionKey> map = new LinkedHashMap<>();
		List<OperatorVo> allOperator = smppService.getAllOperator();
		for (OperatorVo operatorVo : allOperator) {
			SessionKey sessionKey = new SessionKey();
			sessionKey.setSenderId(operatorVo.getSenderid());
			sessionKey.setSystemId(operatorVo.getSystemid());
			map.put(operatorVo.getChannel(), sessionKey);
		}

		CHANNL_REL.clear();
		CHANNL_REL = map;
	}


	/**
	 * 初始化读取配置文件信息
	 */
	private void initSystemGlobals() {
		try {
			PropertiesLoader propertiesLoader = new PropertiesLoader();
			Properties properties = propertiesLoader.getProperties(SystemGlobals.SYSTEM_GLOBALS_NAME);
			SystemGlobals.setProperties(properties);
		} catch (Exception e) {
			logger.error("系统启动，初始化读取配置文件信息失败", e);
		}
	}

}
