package com.zm.gmall.manage.dao;

import com.zm.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface AttrMapper extends Mapper<PmsBaseAttrInfo> {
    public List<PmsBaseAttrInfo> selectPmsBaseAttrInfoByValueId(@Param("valueIdStr") String valueIdStr);
}
