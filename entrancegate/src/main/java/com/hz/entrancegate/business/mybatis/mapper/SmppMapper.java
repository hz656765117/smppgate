package com.hz.entrancegate.business.mybatis.mapper;

import com.hz.entrancegate.business.pojo.OperatorVo;
import com.hz.entrancegate.business.pojo.SmppUserVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmppMapper {


    List<SmppUserVo> selectUser(@Param("list") List<Integer> ids,@Param("userType") Integer userType);

    List<OperatorVo> selectOperator();



}