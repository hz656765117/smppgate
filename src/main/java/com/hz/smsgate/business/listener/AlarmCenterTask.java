package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.utils.HttpClientUtils;
import com.hz.smsgate.base.utils.MailUtil;
import com.hz.smsgate.base.utils.SpringContextUtil;
import com.hz.smsgate.business.pojo.CustomParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 告警中心任务
 *
 * @Auther: huangzhuo
 * @Date: 2020/3/26 10:05
 */
@Component
public class AlarmCenterTask {
	private static final Logger logger = LoggerFactory.getLogger(AlarmCenterTask.class);

	@Autowired
    ClientInit clientInit;

	public static AlarmCenterTask configLoadTask;

	@Value("${server.port}")
	private String port;


	Integer errorSize = 0;

	@PostConstruct
	public void init() {
		configLoadTask = this;
		configLoadTask.clientInit = this.clientInit;

	}


//	@Scheduled(cron = "0/30 * * * * ? ")
//	public void heartbeat() {
//
//		try {
//			CustomParam customParam = SpringContextUtil.getBean(CustomParam.class);
//
//			if (customParam.isHeartBeat()) {
//
//
//				String url = "http://" + customParam.getSmsgateIp() + ":" + customParam.getSmsgatePort() + "/alarmCenter/heartbeat?nodeId=" + customParam.getOperatorType() + "&port=" + port;
//				String result = HttpClientUtils.doGet(url);
//				if (StringUtils.isBlank(result)) {
//					errorSize++;
//					if (errorSize >= 3) {
//						logger.error("第{}次请求短信网关异常,清空错误次数，并休眠{}ms", errorSize, 300000);
//						SpringContextUtil.getBean(MailUtil.class).sendSimpleMail("867178908@qq.com", "smsgate状态异常", customParam.getOperatorType() + "节点访问短信网关异常，请管理员尽快检查短信网关是否异常关闭");
//						Thread.sleep(300000);
//						errorSize = 0;
//					} else {
//						logger.error("第{}次请求短信网关异常", errorSize);
//					}
//				}
//
//
//			}
//
//
//		} catch (Exception e) {
//			logger.error("spgate健康检查异常", e);
//		}
//	}


}
