package com.hz.smsgate.business.service.impl;


import com.hz.smsgate.business.mybatis.mapper.ChannelMapper;
import com.hz.smsgate.business.mybatis.mapper.SmppMapper;
import com.hz.smsgate.business.pojo.Channel;
import com.hz.smsgate.business.pojo.ChannelExample;
import com.hz.smsgate.business.pojo.OperatorVo;
import com.hz.smsgate.business.pojo.SmppUserVo;
import com.hz.smsgate.business.service.SmppService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SmppServiceImpl implements SmppService {


	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private SmppMapper smppMapper;


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


}
