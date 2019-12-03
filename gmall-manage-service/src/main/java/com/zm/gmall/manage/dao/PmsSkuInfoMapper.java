package com.zm.gmall.manage.dao;

import com.zm.gmall.bean.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {
    public List<PmsSkuInfo> selectSkuInfoListCheckBySkuId(String spuId);
}
