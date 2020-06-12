package com.hz.smsgate.business.pojo;

import java.io.Serializable;
import java.util.Date;


/**
 * @Auther: huangzhuo
 * @Date: 2020/6/12 14:46
 * @Description:
 */
public class MsgRelateVo  implements Serializable {

	private static final long serialVersionUID = 1L;


	public String msgId;

	public String spMsgId;

	public String errorCode;

	public Date receiveTime;


	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getSpMsgId() {
		return spMsgId;
	}

	public void setSpMsgId(String spMsgId) {
		this.spMsgId = spMsgId;
	}


	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public MsgRelateVo(String msgId, String spMsgId) {
		this.msgId = msgId;
		this.spMsgId = spMsgId;
	}

	public MsgRelateVo(String spMsgId, String errorCode, Date receiveTime) {
		this.spMsgId = spMsgId;
		this.errorCode = errorCode;
		this.receiveTime = receiveTime;
	}
}
