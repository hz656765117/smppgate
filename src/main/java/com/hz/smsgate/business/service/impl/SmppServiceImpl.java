package com.hz.smsgate.business.service.impl;


import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.utils.ChangeCharset;
import com.hz.smsgate.base.utils.DateUtil;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.business.mybatis.mapper.ChannelMapper;
import com.hz.smsgate.business.mybatis.mapper.MtTaskMapper;
import com.hz.smsgate.business.mybatis.mapper.SmppMapper;
import com.hz.smsgate.business.pojo.*;
import com.hz.smsgate.business.service.SmppService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SmppServiceImpl implements SmppService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmppServiceImpl.class);

	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private SmppMapper smppMapper;


	@Autowired
	private MtTaskMapper mtTaskMapper;

	@Override
	public List<Channel> getAllChannels() {
		ChannelExample example = new ChannelExample();
		example.createCriteria().andSenderidIsNotNull();
		return channelMapper.selectByExample(example);
	}

	@Override
	public List<SmppUserVo> getCmAllSmppUser() {
		return getSmppUserByUserType(0);
	}


	@Override
	public List<SmppUserVo> getHttpAllSmppUser() {
		return getSmppUserByUserType(1);
	}

	/**
	 * 根据用户类型查询所有的接入账号
	 *
	 * @param userType 用户类型 0 cm接入账号  1 http接入账号
	 * @return 接入账号集合
	 */
	private List<SmppUserVo> getSmppUserByUserType(int userType) {

		List<SmppUserVo> smppUserVos = smppMapper.selectUser(null, userType);


		if (smppUserVos != null && smppUserVos.size() > 0) {
			List<SmppUserVo> list;
			for (SmppUserVo smppUserVo : smppUserVos) {
				String userIds = smppUserVo.getUserIds();
				if (StringUtils.isNotBlank(userIds)) {
					List<Integer> listIds = Arrays.asList(userIds.split(",")).stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList());
					list = smppMapper.selectUser(listIds, userType);
					smppUserVo.setSenderid(smppUserVo.getDesc());
					smppUserVo.setChannel(smppUserVo.getDesc());
					smppUserVo.setSystemid(smppUserVo.getSmppUser());
					smppUserVo.setList(list);
				}
			}
		}

		return smppUserVos;
	}

	@Override
	public List<OperatorVo> getAllOperator() {
		return smppMapper.selectOperator();
	}


	@Override
	public boolean insertMtTask(SubmitSm submitSm) {
		int i = 0;
		try {
			MtTask mtTask = new MtTask();

			Date curDate = new Date();

			String mbl = submitSm.getDestAddress().getAddress();
			//获取区号
			String areaCode = PduUtils.getAreaCode(mbl);
			//获取号段
			String numSeg = PduUtils.getNumSeg(mbl);

			byte[] shortMessage = submitSm.getShortMessage();
			String msg = "";
			if (shortMessage[0] == 5 && shortMessage[1] == 0 && shortMessage[2] == 3) {
				mtTask.setLongMsgSeq(submitSm.getSequenceNumber());
				mtTask.setPkNumber((int) shortMessage[5]);
				mtTask.setPkTotal((int) shortMessage[4]);
			}
			msg = new String(shortMessage, ChangeCharset.UTF_8);
			mtTask.setMessage(msg);

			String mm = DateUtil.convertDateToString(curDate, "yyyyMM");
			mtTask.setTableName("t_mt_task_" + mm);

			mtTask.setPhone(mbl);
			mtTask.setRealMsgId(submitSm.getTempMsgId());
			mtTask.setSystemId(submitSm.getSystemId());
			mtTask.setSenderId(submitSm.getSourceAddress().getAddress());
			mtTask.setSendTime(curDate);
			mtTask.setAreaCode(areaCode);
			mtTask.setNumSeg(numSeg);
			i = mtTaskMapper.insertSelective(mtTask);
		} catch (Exception e) {
			LOGGER.error("新增下行明细异常", e);
		}

		return i > 0;
	}
}
