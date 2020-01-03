package com.hz.smsgate.business.mybatis.mapper;

import com.hz.smsgate.business.pojo.Channel;
import com.hz.smsgate.business.pojo.ChannelExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelMapper {
    long countByExample(ChannelExample example);

    int deleteByExample(ChannelExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Channel record);

    int insertSelective(Channel record);

    List<Channel> selectByExample(ChannelExample example);

    Channel selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Channel record, @Param("example") ChannelExample example);

    int updateByExample(@Param("record") Channel record, @Param("example") ChannelExample example);

    int updateByPrimaryKeySelective(Channel record);

    int updateByPrimaryKey(Channel record);
}