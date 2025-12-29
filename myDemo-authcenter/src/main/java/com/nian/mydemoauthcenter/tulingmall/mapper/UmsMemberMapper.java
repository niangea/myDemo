package com.nian.mydemoauthcenter.tulingmall.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.nian.mydemoauthcenter.tulingmall.model.UmsMember;
import com.nian.mydemoauthcenter.tulingmall.model.UmsMemberExample;
import org.apache.ibatis.annotations.Param;


import java.util.List;
@DS("ums_member")
public interface UmsMemberMapper {
    long countByExample(UmsMemberExample example);

    int deleteByExample(UmsMemberExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsMember record);

    int insertSelective(UmsMember record);

    List<UmsMember> selectByExample(UmsMemberExample example);

    UmsMember selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UmsMember record, @Param("example") UmsMemberExample example);

    int updateByExample(@Param("record") UmsMember record, @Param("example") UmsMemberExample example);

    int updateByPrimaryKeySelective(UmsMember record);

    int updateByPrimaryKey(UmsMember record);
}