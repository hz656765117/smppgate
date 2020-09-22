package com.hz.smsgate.business.mybatis.mapper;

import com.hz.smsgate.business.pojo.BindRecord;
import com.hz.smsgate.business.pojo.BindRecordExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BindRecordMapper {
    long countByExample(BindRecordExample example);

    int deleteByExample(BindRecordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(BindRecord record);

    int insertSelective(BindRecord record);

    List<BindRecord> selectByExample(BindRecordExample example);

    BindRecord selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") BindRecord record, @Param("example") BindRecordExample example);

    int updateByExample(@Param("record") BindRecord record, @Param("example") BindRecordExample example);

    int updateByPrimaryKeySelective(BindRecord record);

    int updateByPrimaryKey(BindRecord record);
}