package com.zm.gmall.user.dao;

import com.zm.gmall.bean.UmsMember;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UmsMemberMapper {
    List<UmsMember> selectUser();
    int insertUser(@Param("user") UmsMember user);
    int updateUser(@Param("user") UmsMember user);
    int deleteUserById(String id);
}
