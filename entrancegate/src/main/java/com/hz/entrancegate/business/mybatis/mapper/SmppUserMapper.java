package com.hz.entrancegate.business.mybatis.mapper;

import com.hz.entrancegate.business.pojo.SmppUser;
import com.hz.entrancegate.business.pojo.SmppUserExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmppUserMapper {
    long countByExample(SmppUserExample example);

    int deleteByExample(SmppUserExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SmppUser record);

    int insertSelective(SmppUser record);

    List<SmppUser> selectByExample(SmppUserExample example);

    SmppUser selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SmppUser record, @Param("example") SmppUserExample example);

    int updateByExample(@Param("record") SmppUser record, @Param("example") SmppUserExample example);

    int updateByPrimaryKeySelective(SmppUser record);

    int updateByPrimaryKey(SmppUser record);
}