package com.hz.entrancegate.business.mybatis.mapper;

import com.hz.entrancegate.business.pojo.Operator;
import com.hz.entrancegate.business.pojo.OperatorExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperatorMapper {
    long countByExample(OperatorExample example);

    int deleteByExample(OperatorExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Operator record);

    int insertSelective(Operator record);

    List<Operator> selectByExample(OperatorExample example);

    Operator selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Operator record, @Param("example") OperatorExample example);

    int updateByExample(@Param("record") Operator record, @Param("example") OperatorExample example);

    int updateByPrimaryKeySelective(Operator record);

    int updateByPrimaryKey(Operator record);
}