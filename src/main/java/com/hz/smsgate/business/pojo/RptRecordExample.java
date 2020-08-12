package com.hz.smsgate.business.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RptRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RptRecordExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdIsNull() {
            addCriterion("sp_msg_id is null");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdIsNotNull() {
            addCriterion("sp_msg_id is not null");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdEqualTo(String value) {
            addCriterion("sp_msg_id =", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdNotEqualTo(String value) {
            addCriterion("sp_msg_id <>", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdGreaterThan(String value) {
            addCriterion("sp_msg_id >", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdGreaterThanOrEqualTo(String value) {
            addCriterion("sp_msg_id >=", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdLessThan(String value) {
            addCriterion("sp_msg_id <", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdLessThanOrEqualTo(String value) {
            addCriterion("sp_msg_id <=", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdLike(String value) {
            addCriterion("sp_msg_id like", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdNotLike(String value) {
            addCriterion("sp_msg_id not like", value, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdIn(List<String> values) {
            addCriterion("sp_msg_id in", values, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdNotIn(List<String> values) {
            addCriterion("sp_msg_id not in", values, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdBetween(String value1, String value2) {
            addCriterion("sp_msg_id between", value1, value2, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSpMsgIdNotBetween(String value1, String value2) {
            addCriterion("sp_msg_id not between", value1, value2, "spMsgId");
            return (Criteria) this;
        }

        public Criteria andSystemIdIsNull() {
            addCriterion("system_id is null");
            return (Criteria) this;
        }

        public Criteria andSystemIdIsNotNull() {
            addCriterion("system_id is not null");
            return (Criteria) this;
        }

        public Criteria andSystemIdEqualTo(String value) {
            addCriterion("system_id =", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotEqualTo(String value) {
            addCriterion("system_id <>", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdGreaterThan(String value) {
            addCriterion("system_id >", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdGreaterThanOrEqualTo(String value) {
            addCriterion("system_id >=", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdLessThan(String value) {
            addCriterion("system_id <", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdLessThanOrEqualTo(String value) {
            addCriterion("system_id <=", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdLike(String value) {
            addCriterion("system_id like", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotLike(String value) {
            addCriterion("system_id not like", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdIn(List<String> values) {
            addCriterion("system_id in", values, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotIn(List<String> values) {
            addCriterion("system_id not in", values, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdBetween(String value1, String value2) {
            addCriterion("system_id between", value1, value2, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotBetween(String value1, String value2) {
            addCriterion("system_id not between", value1, value2, "systemId");
            return (Criteria) this;
        }

        public Criteria andSenderidIsNull() {
            addCriterion("senderid is null");
            return (Criteria) this;
        }

        public Criteria andSenderidIsNotNull() {
            addCriterion("senderid is not null");
            return (Criteria) this;
        }

        public Criteria andSenderidEqualTo(String value) {
            addCriterion("senderid =", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidNotEqualTo(String value) {
            addCriterion("senderid <>", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidGreaterThan(String value) {
            addCriterion("senderid >", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidGreaterThanOrEqualTo(String value) {
            addCriterion("senderid >=", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidLessThan(String value) {
            addCriterion("senderid <", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidLessThanOrEqualTo(String value) {
            addCriterion("senderid <=", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidLike(String value) {
            addCriterion("senderid like", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidNotLike(String value) {
            addCriterion("senderid not like", value, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidIn(List<String> values) {
            addCriterion("senderid in", values, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidNotIn(List<String> values) {
            addCriterion("senderid not in", values, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidBetween(String value1, String value2) {
            addCriterion("senderid between", value1, value2, "senderid");
            return (Criteria) this;
        }

        public Criteria andSenderidNotBetween(String value1, String value2) {
            addCriterion("senderid not between", value1, value2, "senderid");
            return (Criteria) this;
        }

        public Criteria andPhoneIsNull() {
            addCriterion("phone is null");
            return (Criteria) this;
        }

        public Criteria andPhoneIsNotNull() {
            addCriterion("phone is not null");
            return (Criteria) this;
        }

        public Criteria andPhoneEqualTo(String value) {
            addCriterion("phone =", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotEqualTo(String value) {
            addCriterion("phone <>", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneGreaterThan(String value) {
            addCriterion("phone >", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("phone >=", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLessThan(String value) {
            addCriterion("phone <", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLessThanOrEqualTo(String value) {
            addCriterion("phone <=", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLike(String value) {
            addCriterion("phone like", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotLike(String value) {
            addCriterion("phone not like", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneIn(List<String> values) {
            addCriterion("phone in", values, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotIn(List<String> values) {
            addCriterion("phone not in", values, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneBetween(String value1, String value2) {
            addCriterion("phone between", value1, value2, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotBetween(String value1, String value2) {
            addCriterion("phone not between", value1, value2, "phone");
            return (Criteria) this;
        }

        public Criteria andStateIsNull() {
            addCriterion("state is null");
            return (Criteria) this;
        }

        public Criteria andStateIsNotNull() {
            addCriterion("state is not null");
            return (Criteria) this;
        }

        public Criteria andStateEqualTo(String value) {
            addCriterion("state =", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateNotEqualTo(String value) {
            addCriterion("state <>", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateGreaterThan(String value) {
            addCriterion("state >", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateGreaterThanOrEqualTo(String value) {
            addCriterion("state >=", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateLessThan(String value) {
            addCriterion("state <", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateLessThanOrEqualTo(String value) {
            addCriterion("state <=", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateLike(String value) {
            addCriterion("state like", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateNotLike(String value) {
            addCriterion("state not like", value, "state");
            return (Criteria) this;
        }

        public Criteria andStateIn(List<String> values) {
            addCriterion("state in", values, "state");
            return (Criteria) this;
        }

        public Criteria andStateNotIn(List<String> values) {
            addCriterion("state not in", values, "state");
            return (Criteria) this;
        }

        public Criteria andStateBetween(String value1, String value2) {
            addCriterion("state between", value1, value2, "state");
            return (Criteria) this;
        }

        public Criteria andStateNotBetween(String value1, String value2) {
            addCriterion("state not between", value1, value2, "state");
            return (Criteria) this;
        }

        public Criteria andStateDesIsNull() {
            addCriterion("state_des is null");
            return (Criteria) this;
        }

        public Criteria andStateDesIsNotNull() {
            addCriterion("state_des is not null");
            return (Criteria) this;
        }

        public Criteria andStateDesEqualTo(String value) {
            addCriterion("state_des =", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesNotEqualTo(String value) {
            addCriterion("state_des <>", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesGreaterThan(String value) {
            addCriterion("state_des >", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesGreaterThanOrEqualTo(String value) {
            addCriterion("state_des >=", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesLessThan(String value) {
            addCriterion("state_des <", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesLessThanOrEqualTo(String value) {
            addCriterion("state_des <=", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesLike(String value) {
            addCriterion("state_des like", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesNotLike(String value) {
            addCriterion("state_des not like", value, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesIn(List<String> values) {
            addCriterion("state_des in", values, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesNotIn(List<String> values) {
            addCriterion("state_des not in", values, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesBetween(String value1, String value2) {
            addCriterion("state_des between", value1, value2, "stateDes");
            return (Criteria) this;
        }

        public Criteria andStateDesNotBetween(String value1, String value2) {
            addCriterion("state_des not between", value1, value2, "stateDes");
            return (Criteria) this;
        }

        public Criteria andErrorCodeIsNull() {
            addCriterion("error_code is null");
            return (Criteria) this;
        }

        public Criteria andErrorCodeIsNotNull() {
            addCriterion("error_code is not null");
            return (Criteria) this;
        }

        public Criteria andErrorCodeEqualTo(String value) {
            addCriterion("error_code =", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotEqualTo(String value) {
            addCriterion("error_code <>", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeGreaterThan(String value) {
            addCriterion("error_code >", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeGreaterThanOrEqualTo(String value) {
            addCriterion("error_code >=", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeLessThan(String value) {
            addCriterion("error_code <", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeLessThanOrEqualTo(String value) {
            addCriterion("error_code <=", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeLike(String value) {
            addCriterion("error_code like", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotLike(String value) {
            addCriterion("error_code not like", value, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeIn(List<String> values) {
            addCriterion("error_code in", values, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotIn(List<String> values) {
            addCriterion("error_code not in", values, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeBetween(String value1, String value2) {
            addCriterion("error_code between", value1, value2, "errorCode");
            return (Criteria) this;
        }

        public Criteria andErrorCodeNotBetween(String value1, String value2) {
            addCriterion("error_code not between", value1, value2, "errorCode");
            return (Criteria) this;
        }

        public Criteria andSubTimeIsNull() {
            addCriterion("sub_time is null");
            return (Criteria) this;
        }

        public Criteria andSubTimeIsNotNull() {
            addCriterion("sub_time is not null");
            return (Criteria) this;
        }

        public Criteria andSubTimeEqualTo(Date value) {
            addCriterion("sub_time =", value, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeNotEqualTo(Date value) {
            addCriterion("sub_time <>", value, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeGreaterThan(Date value) {
            addCriterion("sub_time >", value, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("sub_time >=", value, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeLessThan(Date value) {
            addCriterion("sub_time <", value, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeLessThanOrEqualTo(Date value) {
            addCriterion("sub_time <=", value, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeIn(List<Date> values) {
            addCriterion("sub_time in", values, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeNotIn(List<Date> values) {
            addCriterion("sub_time not in", values, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeBetween(Date value1, Date value2) {
            addCriterion("sub_time between", value1, value2, "subTime");
            return (Criteria) this;
        }

        public Criteria andSubTimeNotBetween(Date value1, Date value2) {
            addCriterion("sub_time not between", value1, value2, "subTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeIsNull() {
            addCriterion("done_time is null");
            return (Criteria) this;
        }

        public Criteria andDoneTimeIsNotNull() {
            addCriterion("done_time is not null");
            return (Criteria) this;
        }

        public Criteria andDoneTimeEqualTo(Date value) {
            addCriterion("done_time =", value, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeNotEqualTo(Date value) {
            addCriterion("done_time <>", value, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeGreaterThan(Date value) {
            addCriterion("done_time >", value, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("done_time >=", value, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeLessThan(Date value) {
            addCriterion("done_time <", value, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeLessThanOrEqualTo(Date value) {
            addCriterion("done_time <=", value, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeIn(List<Date> values) {
            addCriterion("done_time in", values, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeNotIn(List<Date> values) {
            addCriterion("done_time not in", values, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeBetween(Date value1, Date value2) {
            addCriterion("done_time between", value1, value2, "doneTime");
            return (Criteria) this;
        }

        public Criteria andDoneTimeNotBetween(Date value1, Date value2) {
            addCriterion("done_time not between", value1, value2, "doneTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdIsNull() {
            addCriterion("real_msg_id is null");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdIsNotNull() {
            addCriterion("real_msg_id is not null");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdEqualTo(String value) {
            addCriterion("real_msg_id =", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdNotEqualTo(String value) {
            addCriterion("real_msg_id <>", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdGreaterThan(String value) {
            addCriterion("real_msg_id >", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdGreaterThanOrEqualTo(String value) {
            addCriterion("real_msg_id >=", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdLessThan(String value) {
            addCriterion("real_msg_id <", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdLessThanOrEqualTo(String value) {
            addCriterion("real_msg_id <=", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdLike(String value) {
            addCriterion("real_msg_id like", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdNotLike(String value) {
            addCriterion("real_msg_id not like", value, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdIn(List<String> values) {
            addCriterion("real_msg_id in", values, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdNotIn(List<String> values) {
            addCriterion("real_msg_id not in", values, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdBetween(String value1, String value2) {
            addCriterion("real_msg_id between", value1, value2, "realMsgId");
            return (Criteria) this;
        }

        public Criteria andRealMsgIdNotBetween(String value1, String value2) {
            addCriterion("real_msg_id not between", value1, value2, "realMsgId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}