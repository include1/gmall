package com.zm.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zm.gmall.bean.PmsSkuInfo;
import com.zm.gmall.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Column;

@Controller
@CrossOrigin
public class SkuController{
    @Reference
    SkuService skuService;

    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        String success = skuService.saveSkuInfo(pmsSkuInfo);
        return success;
    }
}
