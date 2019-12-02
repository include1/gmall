package com.zm.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zm.gmall.bean.*;
import com.zm.gmall.manage.dao.*;
import com.zm.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;
    //查询商品的信息
    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        return pmsProductInfoMapper.select(pmsProductInfo);
    }
    //实现保存操作
    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
       //首先保存productInfo表中的信息,并返回插入后的主键(infoId)
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        //向productImage表中插入数据
        List<PmsProductImage> pmsProductImageList = pmsProductInfo.getSpuImageList();
        for(PmsProductImage pmsProductImage : pmsProductImageList){
            pmsProductImage.setProductId(pmsProductInfo.getId());
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }
        //向productSaleAttr表中插入信息
        List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        for(PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrList) {
            //向productSaleAttr插入输入
            //插入外键
            productSaleAttr.setProductId(pmsProductInfo.getId());
            pmsProductSaleAttrMapper.insertSelective(productSaleAttr);
            //向productSaleAttrValue表中插入数据
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList = productSaleAttr.getSpuSaleAttrValueList();
            for(PmsProductSaleAttrValue pmsProductSaleAttrValue : pmsProductSaleAttrValueList){
                pmsProductSaleAttrValue.setProductId(pmsProductInfo.getId());
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }
        }
        return "success";
    }
    //查询商品属性列表
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        //通过spuId商品的销售属性
        PmsProductSaleAttr productSaleAttr = new PmsProductSaleAttr();
        productSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr>  pmsProductSaleAttrList = pmsProductSaleAttrMapper.select(productSaleAttr);
        //获取属性列表的插入属性值
        for(PmsProductSaleAttr pmsProductSaleAttr : pmsProductSaleAttrList){
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(pmsProductSaleAttr.getProductId());
            pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            pmsProductSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValueList);
        }
        return pmsProductSaleAttrList;
    }
    //查询商品图片列表
    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImageList = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImageList;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {

        //查询选中商品的同一系列库存的商品属性值，以及选中商品的设置
        List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);
        return pmsProductSaleAttrList;
    }

}
