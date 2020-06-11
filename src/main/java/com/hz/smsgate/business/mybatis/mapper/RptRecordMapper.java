package com.hz.smsgate.business.mybatis.mapper;

import com.hz.smsgate.business.pojo.RptRecord;
import com.hz.smsgate.business.pojo.RptRecordExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RptRecordMapper {
    long countByExample(RptRecordExample example);

    int deleteByExample(RptRecordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RptRecord record);

    int insertSelective(RptRecord record);

    List<RptRecord> selectByExample(RptRecordExample example);

    RptRecord selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RptRecord record, @Param("example") RptRecordExample example);

    int updateByExample(@Param("record") RptRecord record, @Param("example") RptRecordExample example);

    int updateByPrimaryKeySelective(RptRecord record);

    int updateByPrimaryKey(RptRecord record);
}