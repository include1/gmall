package com.zm.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sun.jmx.snmp.internal.SnmpMsgProcessingSubSystem;
import com.zm.gmall.bean.PmsBaseSaleAttr;
import com.zm.gmall.bean.PmsProductImage;
import com.zm.gmall.bean.PmsProductInfo;
import com.zm.gmall.bean.PmsProductSaleAttr;
import com.zm.gmall.manage.util.PmsUploadUtil;
import com.zm.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){
        List<PmsProductImage> pmsProductImageList = spuService.spuImageList(spuId);
        return pmsProductImageList;
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrList = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrList;
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        return spuService.spuList(catalog3Id);
    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        String success = spuService.saveSpuInfo(pmsProductInfo);
        return success;
    }
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        //上传的文件服务器并返回地址
        String imgUrl = PmsUploadUtil.uploaadImage(multipartFile);
        System.out.println(imgUrl);
        return imgUrl;
    }
}
