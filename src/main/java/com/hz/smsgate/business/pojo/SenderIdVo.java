package com.hz.smsgate.business.pojo;


import java.io.Serializable;

/**
 * @author huangzhuo
 * @date 2020/1/19 15:17
 */
public class SenderIdVo implements Serializable {
	private static final long serialVersionUID = 1L;

	public String msgId;

	public String systemId;

	public String channel;

	public String realChannel;


	public SenderIdVo() {
	}

	public SenderIdVo(String msgId, String systemId, String channel, String realChannel) {
		this.msgId = msgId;
		this.systemId = systemId;
		this.channel = channel;
		this.realChannel = realChannel;
	}


	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getRealChannel() {
		return realChannel;
	}

	public void setRealChannel(String realChannel) {
		this.realChannel = realChannel;
	}
}
