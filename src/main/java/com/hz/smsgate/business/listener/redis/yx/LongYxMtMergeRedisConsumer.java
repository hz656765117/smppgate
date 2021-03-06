package com.hz.smsgate.business.listener.redis.yx;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;


/**
 * 长短信合并
 *
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
@Component
public class LongYxMtMergeRedisConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongYxMtMergeRedisConsumer.class);

	@Autowired
	public RedisUtil redisUtil;

	public static LongYxMtMergeRedisConsumer longMtMergeRedisConsumer;

	@PostConstruct
	public void init() {
		longMtMergeRedisConsumer = this;
		longMtMergeRedisConsumer.redisUtil = this.redisUtil;
	}

	private static final Map<String, SubmitSm> CACHE_MAP = new LinkedHashMap<>();


	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}
		SubmitSm submitSm;
		LOGGER.info("{}-长短信（redis）合并线程开始工作......", Thread.currentThread().getName());

		while (true) {


			try {
				if (longMtMergeRedisConsumer.redisUtil != null) {
					Object obj = longMtMergeRedisConsumer.redisUtil.rPop(SmppServerConstants.WEB_LONG_SUBMIT_SM_YX);
					if (obj != null) {
						submitSm = (SubmitSm) obj;
						validateMt(submitSm);
						mergeSt();
					} else {
						Thread.sleep(1000);
					}
				} else {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				LOGGER.error("{}-长短信（redis）合并异常", Thread.currentThread().getName(), e);
				try {
					Thread.sleep(10000);
				}catch (Exception E){

				}
			}

		}

	}


	public void validateMt(SubmitSm submitSm) throws Exception {
		byte[] shortMessage = submitSm.getShortMessage();

		byte[] test = new byte[6];
		byte[] realmsg = new byte[shortMessage.length - 6];
		System.arraycopy(shortMessage, 0, test, 0, 6);
		System.arraycopy(shortMessage, 6, realmsg, 0, shortMessage.length - 6);
		submitSm.setShortMessage(realmsg);
		submitSm.setCommandLength(submitSm.getCommandLength() - (shortMessage.length - realmsg.length));


		StringBuilder key = new StringBuilder();
		key.append(submitSm.getSourceAddress().getAddress());
		key.append("-");
		key.append(submitSm.getDestAddress().getAddress());
		key.append("-");
		key.append(shortMessage[4]);
		key.append("-");
		key.append(shortMessage[5]);

		CACHE_MAP.put(key.toString(), submitSm);
	}


	public void mergeSt() throws Exception {

		if (CACHE_MAP.size() <= 0) {
			return;
		}

		Map<String, SubmitSm> tempMap = new LinkedHashMap<>();
		Map<String, SubmitSm> completeMap = new LinkedHashMap<>();
		boolean flag = true;
		String tempKey = "";
		for (Map.Entry<String, SubmitSm> entry : CACHE_MAP.entrySet()) {
			String key = entry.getKey();
			if ("1".equals(key.substring(key.length() - 1)) && flag) {
				tempKey = key;
				flag = false;
				completeMap.put(entry.getKey(), entry.getValue());
			} else {
				if (!StringUtils.isBlank(tempKey) && key.substring(0, key.length() - 1).equals(tempKey.substring(0, tempKey.length() - 1))) {
					completeMap.put(entry.getKey(), entry.getValue());
					String[] split = key.split("-");
					String msgCount = split[split.length - 2];

					if (completeMap.size() == Integer.valueOf(msgCount)) {
						SubmitSm submitSm = mergeSubmitSm(completeMap);
						if (submitSm != null) {
							tempKey = "";
							flag = true;
							try {
								longMtMergeRedisConsumer.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_SEND_YX, submitSm);
							} catch (Exception e) {
								LOGGER.error("-----------短信下行（长短信合并），加入队列异常。------------- {}", e);
							}
							completeMap.clear();
						} else {
							//如果合并失败则把各条短信放到临时map中，下次再合并
							tempMap.putAll(completeMap);
						}
					}


				} else {
					tempMap.put(entry.getKey(), entry.getValue());
				}
			}
		}


		CACHE_MAP.clear();
		if (completeMap.size() > 0) {
			tempMap.putAll(completeMap);
		}

		if (tempMap != null && tempMap.size() > 0) {
			List<Map.Entry<String, SubmitSm>> hashList = new ArrayList<Map.Entry<String, SubmitSm>>(tempMap.entrySet());
			Collections.sort(hashList, new Comparator<Map.Entry<String, SubmitSm>>() {
				// 升序排序
				@Override
				public int compare(Map.Entry<String, SubmitSm> o1, Map.Entry<String, SubmitSm> o2) {
					return o1.getKey().compareTo(o2.getKey());
				}
			});
			// 排序后输出
			for (Map.Entry<String, SubmitSm> m : hashList) {
				CACHE_MAP.put(m.getKey(), m.getValue());
			}
		}


	}


	public SubmitSm mergeSubmitSm(Map<String, SubmitSm> tempMap1) throws Exception {
		SubmitSm mt = null;
		String tempKey = "";
		for (Map.Entry<String, SubmitSm> entry : tempMap1.entrySet()) {
			String key = entry.getKey();
			if ("1".equals(key.substring(key.length() - 1))) {
				mt = entry.getValue();
				tempKey = key;
			}
		}

		if (mt != null) {
			String[] split = tempKey.split("-");
			String preKey = tempKey.substring(0, tempKey.length() - 1);
			int msgCount = Integer.valueOf(split[split.length - 2]);


			//计算短信内容长度
			int shortMessageLen = 0;
			for (int i = 1; i <= msgCount; i++) {
				String key = preKey + i;
				SubmitSm submitSm = tempMap1.get(key);
				byte[] shortMessage = submitSm.getShortMessage();
				shortMessageLen += shortMessage.length;
			}
			byte[] sm = new byte[shortMessageLen];

			String tempMsgIds = "";

			//拼接短信内容
			int startIndex = 0;
			for (int i = 1; i <= msgCount; i++) {
				String key = preKey + i;
				SubmitSm submitSm = tempMap1.get(key);
				byte[] shortMessage = submitSm.getShortMessage();
				System.arraycopy(shortMessage, 0, sm, startIndex, shortMessage.length);
				startIndex += shortMessage.length;
				tempMsgIds += submitSm.getTempMsgId() + "|";
			}
			LOGGER.info("合并后的内容为{}", new String(sm));

			mt.setShortMessage(sm);
			mt.calculateAndSetCommandLength();

			mt.setTempMsgId(tempMsgIds.substring(0, tempMsgIds.length() - 1));

		}
		return mt;
	}

}
