package com.hz.smsgate.business.pojo;

import java.io.Serializable;
import java.util.Date;

public class BindRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * INTEGER(10) 必填<br>
     * 主键id
     */
    private Integer id;

    /**
     * VARCHAR(64)<br>
     * 运营商
     */
    private String systemid;

    /**
     * VARCHAR(255)<br>
     * 上游ip
     */
    private String ip;

    /**
     * VARCHAR(255)<br>
     * 上游端口
     */
    private String port;

    /**
     * INTEGER(10)<br>
     * 类型 0 bind  1 unbind
     */
    private Integer type;

    /**
     * INTEGER(10) 默认值[0]<br>
     * 状态 0 成功   1 失败
     */
    private Integer status;

    /**
     * TIMESTAMP(19)<br>
     * 时间
     */
    private Date time;

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
     * 获得 运营商
     */
    public String getSystemid() {
        return systemid;
    }

    /**
     * VARCHAR(64)<br>
     * 设置 运营商
     */
    public void setSystemid(String systemid) {
        this.systemid = systemid == null ? null : systemid.trim();
    }

    /**
     * VARCHAR(255)<br>
     * 获得 上游ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * VARCHAR(255)<br>
     * 设置 上游ip
     */
    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    /**
     * VARCHAR(255)<br>
     * 获得 上游端口
     */
    public String getPort() {
        return port;
    }

    /**
     * VARCHAR(255)<br>
     * 设置 上游端口
     */
    public void setPort(String port) {
        this.port = port == null ? null : port.trim();
    }

    /**
     * INTEGER(10)<br>
     * 获得 类型 0 bind  1 unbind
     */
    public Integer getType() {
        return type;
    }

    /**
     * INTEGER(10)<br>
     * 设置 类型 0 bind  1 unbind
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * INTEGER(10) 默认值[0]<br>
     * 获得 状态 0 成功   1 失败
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * INTEGER(10) 默认值[0]<br>
     * 设置 状态 0 成功   1 失败
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * TIMESTAMP(19)<br>
     * 获得 时间
     */
    public Date getTime() {
        return time;
    }

    /**
     * TIMESTAMP(19)<br>
     * 设置 时间
     */
    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", systemid=").append(systemid);
        sb.append(", ip=").append(ip);
        sb.append(", port=").append(port);
        sb.append(", type=").append(type);
        sb.append(", status=").append(status);
        sb.append(", time=").append(time);
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
        BindRecord other = (BindRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getSystemid() == null ? other.getSystemid() == null : this.getSystemid().equals(other.getSystemid()))
            && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
            && (this.getPort() == null ? other.getPort() == null : this.getPort().equals(other.getPort()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getTime() == null ? other.getTime() == null : this.getTime().equals(other.getTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSystemid() == null) ? 0 : getSystemid().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getPort() == null) ? 0 : getPort().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getTime() == null) ? 0 : getTime().hashCode());
        return result;
    }
}