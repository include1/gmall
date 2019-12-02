package com.zm.gmall.user.dao;

import com.zm.gmall.bean.UmsMember;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface UmsMemberMapper extends Mapper<UmsMember> {
    List<UmsMember> selectUser();
    int insertUser(@Param("user") UmsMember user);
    int updateUser(@Param("user") UmsMember user);
    int deleteUserById(String id);
}
