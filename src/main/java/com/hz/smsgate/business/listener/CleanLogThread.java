package com.hz.smsgate.business.listener;

import com.hz.smsgate.base.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

/**
 * @Auther: huangzhuo
 * @Date: 2019/12/21 11:25
 * @Description:
 */
public class CleanLogThread implements Runnable {
	private static Logger LOGGER = LoggerFactory.getLogger(CleanLogThread.class);

	@Override
	public void run() {
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			LOGGER.error("{}-线程启动异常", Thread.currentThread().getName(), e);
		}

		while (true) {

			try {
				deleteLog("/home/pkg/smsgate01/logger", 2);
				deleteLog("/home/pkg/smppServer/logger", 2);
				Thread.sleep(3000000);
			} catch (Exception e) {
				LOGGER.error("{}-处理定时加载数据库中的配置到内存中异常", Thread.currentThread().getName(), e);
				try {
					Thread.sleep(10000);
				}catch (Exception E){

				}
			}

		}


	}


	public static void deleteLog(String path, int saveDay) {
		try {
			String filedir = path;
			int num = saveDay;
			File dirFile = new File(filedir);
			if (dirFile.isDirectory()) {
				long allSize = 0L;
				LOGGER.info("开始删除路径{}下{}天以前的日志", filedir, num);
				File[] files = dirFile.listFiles();
				for (File file : files) {
					String name = file.getName();
					String[] split = name.split("\\.");
					String date = split[split.length - 1];
					if (date.length() >= 10) {
						String curTime = DateUtil.convertDateToString(new Date(), DateUtil.dataFormatyyyy_MM_dd);
						int between = DateUtil.calculateDateBetweenDays(date, curTime, DateUtil.dataFormatyyyy_MM_dd);
						if (between >= num) {
							allSize = allSize + file.length();
							LOGGER.info("删除日志文件 {}，size：{}", file.getAbsoluteFile(), getPrintSize(file.length()));
							file.delete();
						}
					}
				}
				LOGGER.info("删除路径{}下{}天以前的日志完成，总共删除size：{}", filedir, num, getPrintSize(allSize));
			}
		} catch (Exception e) {
			LOGGER.error("删除路径{}下{}天以前的日志异常", path, saveDay, e);
		}
	}

	/**
	 *      * 获取文件大小
	 *      * 
	 *      * @param size
	 *      * @return
	 *     
	 */
	public static String getPrintSize(long size) {

		try {
			// 如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
			if (size < 1024) {
				return String.valueOf(size) + "B";
			} else {
				size = size / 1024;
			}
			// 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
			// 因为还没有到达要使用另一个单位的时候
			// 接下去以此类推
			if (size < 1024) {
				return String.valueOf(size) + "KB";
			} else {
				size = size / 1024;
			}
			if (size < 1024) {
				// 因为如果以MB为单位的话，要保留最后1位小数，
				// 因此，把此数乘以100之后再取余
				size = size * 100;
				return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "MB";
			} else {
				// 否则如果要以GB为单位的，先除于1024再作同样的处理
				size = size * 100 / 1024;
				return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "GB";
			}
		} catch (Exception e) {
			return "0";
		}

	}


}
