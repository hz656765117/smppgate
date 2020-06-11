package com.hz.smsgate.business.service;


import com.hz.smsgate.base.smpp.pdu.DeliverSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.business.pojo.Channel;
import com.hz.smsgate.business.pojo.OperatorVo;
import com.hz.smsgate.business.pojo.SmppUserVo;

import java.util.List;

public interface SmppService {



    List<Channel> getAllChannels();

    /**
     * 查询所有cm的接入账号
     * @return cm的接入账号集合
     */
    List<SmppUserVo> getCmAllSmppUser();


    /**
     * 查询所有http的接入账号
     * @return http的接入账号集合
     */
    List<SmppUserVo> getHttpAllSmppUser();

    /**
     * 查询所有的运营商
     * @return 运营商集合
     */
    List<OperatorVo> getAllOperator();


    boolean insertMtTask(SubmitSm submitSm);

    /**
     * 新增状态报告信息
     * @param deliverSm
     * @return
     */
    boolean insertRptRecord(DeliverSm deliverSm);

}
