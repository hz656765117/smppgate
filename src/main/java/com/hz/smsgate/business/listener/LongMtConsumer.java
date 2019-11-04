package com.hz.smsgate.business.listener;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.je.BDBStoredMapFactoryImpl;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.utils.MsgId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;


/**
 * 长短信合并
 * @author huangzhuo
 * @date 2019/7/2 15:53
 */
public class LongMtConsumer implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(LongMtConsumer.class);

	public static final Map<String, SubmitSm> CACHE_MAP = new LinkedHashMap<>();

//	public static final List<SubmitSm> sendlist = new LinkedList<>();

	@Override
	public void run() {
		SubmitSm submitSm;


		while (true) {

			BlockingQueue<Object> queue = null;
			try {
				queue = BDBStoredMapFactoryImpl.INS.getQueue("longSubmitSm", "longSubmitSm");
			} catch (Exception e) {
				LOGGER.error("{}-获取je队列异常", Thread.currentThread().getName(), e);
			}

			try {
				if (queue != null) {
					Object obj = queue.poll();
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
				LOGGER.error("{}-处理短信状态报告转发异常", Thread.currentThread().getName(), e);
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



	public static String getSuffixKeyBySm(SubmitSm submitSm) {
		byte[] shortMessage = submitSm.getShortMessage();
		StringBuilder key = new StringBuilder();

		if (shortMessage != null && shortMessage.length >= 6) {
			key.append("-");
			key.append(shortMessage[4]);
			key.append("-");
			key.append(shortMessage[5]);
		}

		key.append("-");
		key.append(submitSm.getSequenceNumber());

		return key.toString();
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
								BlockingQueue<Object> queue = BDBStoredMapFactoryImpl.INS.getQueue("longSubmitSmSend", "longSubmitSmSend");
								queue.put(submitSm);
							} catch (Exception e) {
								LOGGER.error("-----------后短信下行（长短信合并），加入队列异常。------------- {}", e);
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
