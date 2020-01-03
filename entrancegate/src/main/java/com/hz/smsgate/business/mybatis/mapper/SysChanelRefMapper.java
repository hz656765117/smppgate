package com.hz.smsgate.business.mybatis.mapper;

import com.hz.smsgate.business.pojo.SysChanelRef;
import com.hz.smsgate.business.pojo.SysChanelRefExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysChanelRefMapper {
    long countByExample(SysChanelRefExample example);

    int deleteByExample(SysChanelRefExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SysChanelRef record);

    int insertSelective(SysChanelRef record);

    List<SysChanelRef> selectByExample(SysChanelRefExample example);

    SysChanelRef selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SysChanelRef record, @Param("example") SysChanelRefExample example);

    int updateByExample(@Param("record") SysChanelRef record, @Param("example") SysChanelRefExample example);

    int updateByPrimaryKeySelective(SysChanelRef record);

    int updateByPrimaryKey(SysChanelRef record);
}