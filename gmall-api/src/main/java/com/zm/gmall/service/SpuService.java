package com.zm.gmall.service;

import com.zm.gmall.bean.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SpuService {
    public List<PmsProductInfo> spuList(String catalog3Id);

    public String saveSpuInfo(PmsProductInfo pmsProductInfo);

    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    public List<PmsProductImage> spuImageList(String spuId);

    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);


}
