package com.zm.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.zm.gmall.bean.*;
import com.zm.gmall.service.SkuService;
import com.zm.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {
    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    @RequestMapping("index")
    public String index(ModelMap modelMap){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("循环数据"+ i);
        }
        modelMap.put("list",list);
        modelMap.put("msg","这是一个thmeleft的模板测试");
        modelMap.put("check","1");
        return "index";
    }
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map, HttpServletRequest request){
        //获取请求ip
        String remoteAddr = request.getRemoteAddr();

        PmsSkuInfo pmsSkuInfo = skuService.getSkuInfoByIdFromRedis(skuId,remoteAddr);
        //sku对象信息
        map.put("skuInfo",pmsSkuInfo);
        //spu属性列表信息
        List<PmsProductSaleAttr> pmsProductSaleAttrList = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getSpuId(),pmsSkuInfo.getId());
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrList);
        //t通过查询skuId,查询同类型全部销售属性值的id

        List<PmsSkuInfo> pmsSkuInfoList = skuService.getSkuInfoListCheckBySkuId(pmsSkuInfo.getSpuId());
        HashMap<String,String> hashMap = new HashMap<>();
        for(PmsSkuInfo skuInfo : pmsSkuInfoList){
            //获取value值
            String v = skuInfo.getId();
            String k = "";
            List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for(PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValueList){
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";
            }
            hashMap.put(k,v);
        }
        //把hasmap对象转化成json字符串传给前端页面
        String jsonStr = JSON.toJSONString(hashMap);
        map.put("SkuAttrValueListJsonStr",jsonStr);
        return "item";

    }
}
