package com.zm.gmall.manage.dao;

import com.zm.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    public List<PmsProductSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("productId") String productId, @Param("skuId") String skuId);
}
