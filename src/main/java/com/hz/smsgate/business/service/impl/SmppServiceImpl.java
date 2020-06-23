package com.hz.smsgate.business.service.impl;


import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.utils.DeliveryReceipt;
import com.hz.smsgate.base.utils.ChangeCharset;
import com.hz.smsgate.base.utils.DateUtil;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.business.mybatis.mapper.ChannelMapper;
import com.hz.smsgate.business.mybatis.mapper.MtTaskMapper;
import com.hz.smsgate.business.mybatis.mapper.RptRecordMapper;
import com.hz.smsgate.business.mybatis.mapper.SmppMapper;
import com.hz.smsgate.business.pojo.*;
import com.hz.smsgate.business.service.SmppService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
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


    @Autowired
    private RptRecordMapper rptRecordMapper;

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

        List<SmppUserVo> smppUserVos = smppMapper.selectFatherUser(userType);


        if (smppUserVos != null && smppUserVos.size() > 0) {
            List<SmppUserVo> list;
            for (SmppUserVo smppUserVo : smppUserVos) {
                String userIds = smppUserVo.getSonSmppUsers();
                if (StringUtils.isNotBlank(userIds)) {
                    List<Integer> listIds = Arrays.asList(userIds.split(",")).stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList());
                    list = smppMapper.selectUser(listIds, userType);
                    smppUserVo.setSenderid(smppUserVo.getSmppChannel());
                    smppUserVo.setChannel(smppUserVo.getSmppChannel());
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

            mtTask.setChannel(submitSm.getChannel());
            mtTask.setPhone(mbl);
            mtTask.setRealMsgId(submitSm.getTempMsgId());
            mtTask.setSystemId(submitSm.getSystemId());
            mtTask.setSenderId(submitSm.getSourceAddress().getAddress());
            mtTask.setSendTime(curDate);
            mtTask.setAreaCode(areaCode);
            mtTask.setNumSeg(numSeg);
            mtTask.setUserId(submitSm.getSmppUser());
            mtTask.setSendType(submitSm.getUserType());
            i = mtTaskMapper.insertSelective(mtTask);
        } catch (Exception e) {
            LOGGER.error("新增下行明细异常", e);
        }

        return i > 0;
    }


    @Override
    public boolean insertRptRecord(DeliverSm deliverSm) {
        RptRecord record = new RptRecord();
        Date curDate = new Date();
        String mm = DateUtil.convertDateToString(curDate, "yyyyMM");
        record.setTableName("t_rpt_record_" + mm);
        int i = 0;
        String spMsgId = "";
        String errrorCode = "";
        try {
            record.setSystemId(deliverSm.getSystemId());
            record.setPhone(deliverSm.getSourceAddress().getAddress());
            record.setSenderid(deliverSm.getDestAddress().getAddress());
            record.setCreateTime(curDate);
            byte[] shortMessage = deliverSm.getShortMessage();
            DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(new String(shortMessage), DateTimeZone.UTC, false);
            record.setSpMsgId(deliveryReceipt.getMessageId());
            spMsgId = deliveryReceipt.getMessageId();
            record.setStateDes(DeliveryReceipt.toStateText(deliveryReceipt.getState()));
            if (deliveryReceipt.getSubmitDate() != null) {
                long subMillis = deliveryReceipt.getSubmitDate().getMillis();
                Date subDate = new Date(subMillis);
                record.setSubTime(subDate);
            }

            if (deliveryReceipt.getDoneDate() != null) {
                long doneMillis = deliveryReceipt.getDoneDate().getMillis();
                Date doneDate = new Date(doneMillis);
                record.setDoneTime(doneDate);
            }

            errrorCode = DeliveryReceipt.toStateText(deliveryReceipt.getState());
            record.setErrorCode(deliveryReceipt.getErrorCode() + "");
            record.setState(deliveryReceipt.getState() + "");
            i = rptRecordMapper.insertSelective(record);
        } catch (Exception e) {
            LOGGER.error("新增状态报告异常", e);
        }


        try {
            if (StringUtils.isNotBlank(spMsgId)) {
                MtTask mttask = new MtTask();
                mttask.setTableName("t_mt_task_" + mm);
                mttask.setSpMsgId(spMsgId);
                mttask.setReceiveFlag(1);
                mttask.setReceiveTime(curDate);
                mttask.setErrorCode(errrorCode);
                mtTaskMapper.updateBySpMsgIdSelective(mttask);
            }
        } catch (Exception e) {
            LOGGER.error("根据spMsgId更新mttask异常", e);
        }


        return i > 0;
    }


    @Override
    public boolean updateMtTaskByMsgId(MsgRelateVo msgRelateVo) {


        MtTask record = new MtTask();
        Date curDate = new Date();
        String mm = DateUtil.convertDateToString(curDate, "yyyyMM");
        record.setTableName("t_mt_task_" + mm);
        int i = 0;

        try {
            record.setRealMsgId(msgRelateVo.getMsgId());
            record.setSpMsgId(msgRelateVo.getSpMsgId());
            record.setSendFlag(1);
            i = mtTaskMapper.updateByRealMsgIdSelective(record);
        } catch (Exception e) {
            LOGGER.error("新增状态报告异常", e);
        }

        return i > 0;
    }

    @Override
    public boolean updateMtTaskBySpMsgId(MsgRelateVo msgRelateVo) {
//		MtTask record = new MtTask();
//		Date curDate = new Date();
//		String mm = DateUtil.convertDateToString(curDate, "yyyyMM");
//		record.setTableName("t_mt_task_" + mm);
//		int i = 0;
//
//		try {
//			record.setRealMsgId(msgRelateVo.getMsgId());
//			record.setSpMsgId(msgRelateVo.getSpMsgId());
//			record.setSendFlag(1);
//			i = mtTaskMapper.updateBySpMsgIdSelective(record);
//		} catch (Exception e) {
//			LOGGER.error("新增状态报告异常", e);
//		}

        return true;
    }
}
