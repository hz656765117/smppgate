package com.hz.smsgate.business.pojo;

import java.io.Serializable;
import java.util.Date;

public class RptRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * INTEGER(10) 必填<br>
     * 主键id
     */
    private Integer id;

    /**
     * VARCHAR(64)<br>
     * 供应商msgid
     */
    private String spMsgId;

    /**
     * VARCHAR(32)<br>
     * 运营商id
     */
    private String systemId;

    /**
     * VARCHAR(32)<br>
     * senderid
     */
    private String senderid;

    /**
     * VARCHAR(20)<br>
     * 手机号码
     */
    private String phone;

    /**
     * VARCHAR(2)<br>
     * 状态
     */
    private String state;

    /**
     * VARCHAR(255)<br>
     * 状态描述
     */
    private String stateDes;

    /**
     * VARCHAR(255)<br>
     * 错误码
     */
    private String errorCode;

    /**
     * TIMESTAMP(19)<br>
     * 发送时间
     */
    private Date subTime;

    /**
     * TIMESTAMP(19)<br>
     * 接受时间
     */
    private Date doneTime;

    /**
     * TIMESTAMP(19)<br>
     * 新增时间
     */
    private Date createTime;

    /**
     * VARCHAR(64)<br>
     * 自己的msgid
     */
    private String realMsgId;


    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * INTEGER(10) 必填<br>
     * 获得 主键id
     */
    public Integer getId() {
        return id;
    }

    /**
     * INTEGER(10) 必填<br>
     * 设置 主键id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * VARCHAR(64)<br>
     * 获得 供应商msgid
     */
    public String getSpMsgId() {
        return spMsgId;
    }

    /**
     * VARCHAR(64)<br>
     * 设置 供应商msgid
     */
    public void setSpMsgId(String spMsgId) {
        this.spMsgId = spMsgId == null ? null : spMsgId.trim();
    }

    /**
     * VARCHAR(32)<br>
     * 获得 运营商id
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * VARCHAR(32)<br>
     * 设置 运营商id
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId == null ? null : systemId.trim();
    }

    /**
     * VARCHAR(32)<br>
     * 获得 senderid
     */
    public String getSenderid() {
        return senderid;
    }

    /**
     * VARCHAR(32)<br>
     * 设置 senderid
     */
    public void setSenderid(String senderid) {
        this.senderid = senderid == null ? null : senderid.trim();
    }

    /**
     * VARCHAR(20)<br>
     * 获得 手机号码
     */
    public String getPhone() {
        return phone;
    }

    /**
     * VARCHAR(20)<br>
     * 设置 手机号码
     */
    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    /**
     * VARCHAR(2)<br>
     * 获得 状态
     */
    public String getState() {
        return state;
    }

    /**
     * VARCHAR(2)<br>
     * 设置 状态
     */
    public void setState(String state) {
        this.state = state == null ? null : state.trim();
    }

    /**
     * VARCHAR(255)<br>
     * 获得 状态描述
     */
    public String getStateDes() {
        return stateDes;
    }

    /**
     * VARCHAR(255)<br>
     * 设置 状态描述
     */
    public void setStateDes(String stateDes) {
        this.stateDes = stateDes == null ? null : stateDes.trim();
    }

    /**
     * VARCHAR(255)<br>
     * 获得 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * VARCHAR(255)<br>
     * 设置 错误码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode == null ? null : errorCode.trim();
    }

    /**
     * TIMESTAMP(19)<br>
     * 获得 发送时间
     */
    public Date getSubTime() {
        return subTime;
    }

    /**
     * TIMESTAMP(19)<br>
     * 设置 发送时间
     */
    public void setSubTime(Date subTime) {
        this.subTime = subTime;
    }

    /**
     * TIMESTAMP(19)<br>
     * 获得 接受时间
     */
    public Date getDoneTime() {
        return doneTime;
    }

    /**
     * TIMESTAMP(19)<br>
     * 设置 接受时间
     */
    public void setDoneTime(Date doneTime) {
        this.doneTime = doneTime;
    }

    /**
     * TIMESTAMP(19)<br>
     * 获得 新增时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * TIMESTAMP(19)<br>
     * 设置 新增时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * VARCHAR(64)<br>
     * 获得 自己的msgid
     */
    public String getRealMsgId() {
        return realMsgId;
    }

    /**
     * VARCHAR(64)<br>
     * 设置 自己的msgid
     */
    public void setRealMsgId(String realMsgId) {
        this.realMsgId = realMsgId == null ? null : realMsgId.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", spMsgId=").append(spMsgId);
        sb.append(", systemId=").append(systemId);
        sb.append(", senderid=").append(senderid);
        sb.append(", phone=").append(phone);
        sb.append(", state=").append(state);
        sb.append(", stateDes=").append(stateDes);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", subTime=").append(subTime);
        sb.append(", doneTime=").append(doneTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", realMsgId=").append(realMsgId);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        RptRecord other = (RptRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getSpMsgId() == null ? other.getSpMsgId() == null : this.getSpMsgId().equals(other.getSpMsgId()))
            && (this.getSystemId() == null ? other.getSystemId() == null : this.getSystemId().equals(other.getSystemId()))
            && (this.getSenderid() == null ? other.getSenderid() == null : this.getSenderid().equals(other.getSenderid()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
            && (this.getStateDes() == null ? other.getStateDes() == null : this.getStateDes().equals(other.getStateDes()))
            && (this.getErrorCode() == null ? other.getErrorCode() == null : this.getErrorCode().equals(other.getErrorCode()))
            && (this.getSubTime() == null ? other.getSubTime() == null : this.getSubTime().equals(other.getSubTime()))
            && (this.getDoneTime() == null ? other.getDoneTime() == null : this.getDoneTime().equals(other.getDoneTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getRealMsgId() == null ? other.getRealMsgId() == null : this.getRealMsgId().equals(other.getRealMsgId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSpMsgId() == null) ? 0 : getSpMsgId().hashCode());
        result = prime * result + ((getSystemId() == null) ? 0 : getSystemId().hashCode());
        result = prime * result + ((getSenderid() == null) ? 0 : getSenderid().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        result = prime * result + ((getStateDes() == null) ? 0 : getStateDes().hashCode());
        result = prime * result + ((getErrorCode() == null) ? 0 : getErrorCode().hashCode());
        result = prime * result + ((getSubTime() == null) ? 0 : getSubTime().hashCode());
        result = prime * result + ((getDoneTime() == null) ? 0 : getDoneTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getRealMsgId() == null) ? 0 : getRealMsgId().hashCode());
        return result;
    }
}