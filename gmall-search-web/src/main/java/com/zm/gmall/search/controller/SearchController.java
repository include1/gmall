package com.zm.gmall.search.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.zm.gmall.annotations.LoginRequired;
import com.zm.gmall.bean.*;
import com.zm.gmall.service.AttrService;
import com.zm.gmall.service.SearchServcie;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.*;

@Controller
@CrossOrigin
public class SearchController {
    @Reference
    SearchServcie searchServcie;
    @Reference
    AttrService attrService;
    //测试页面
    @RequestMapping("index")
    // @LoginRequired(loginSuccess=false)，该注解表示要经过拦截器，验证token,并写入cookie中
    @LoginRequired(loginSuccess = false)
    public String index(){
        return "index";
    }
    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){//关键字，catalog3Id
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList =  searchServcie.list(pmsSearchParam);
       //获取平台属性值的集合
        Set<String> valueIdSet = new HashSet<>();
        for(PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList){
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for(PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList){
                String valueId1 = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId1);
            }
        }
        //显示商品信息列表
        modelMap.put("skuLsInfoList",pmsSearchSkuInfoList);
        //通过属性值，获取属性组列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.getAttrValueByValueId(valueIdSet);
        modelMap.put("attrList",pmsBaseAttrInfoList);
        //通过属性值，删除属性组
        String[] delValueIds = pmsSearchParam.getValueId();
        //创建面包屑的对象集合
        List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
        if(delValueIds != null) {
            //遍历放置属性值Id的数组
            for (String s : delValueIds) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //添加valueId
                pmsSearchCrumb.setValueId(s);
                //添加url地址
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam,s));
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();
            while (iterator.hasNext()) {
                List<PmsBaseAttrValue> attrValueList = iterator.next().getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                    String attrValueIdId = pmsBaseAttrValue.getId();
                        if (s.equals(attrValueIdId)) {
                            //保留属性值名
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }

                //把pmsSearchCrub加入集合中
                    pmsSearchCrumbList.add(pmsSearchCrumb);
            }
        }
        //显示品牌专区
        List<PmsBrand> pmsBrandList = searchServcie.getPmsBrandList();
        modelMap.put("pmsBrand",pmsBrandList);
        //编写urlParam
        String urlParam = getUrlParam(pmsSearchParam,null);
        modelMap.put("urlParam",urlParam);
        //编写关键字
        modelMap.put("keyword",pmsSearchParam.getKeyword());
        //编写面包屑
        modelMap.put("attrValueSelectedList",pmsSearchCrumbList);
        return "list";
    }
      //String ... delValueId:可变形参，类似于数组
    private String getUrlParam(PmsSearchParam pmsSearchParam,String  delValueId) {
        String urlParam = "";
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String [] pmsSkuAttrValueList = pmsSearchParam.getValueId();

        if(!StringUtils.isBlank(keyword)){
               //判断是否前面是否为空
            if(!StringUtils.isBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword="+keyword;
        }
        if(!StringUtils.isBlank(catalog3Id)){
            if(!StringUtils.isBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if(pmsSkuAttrValueList != null){
            for (String valueId : pmsSkuAttrValueList) {
                //判断是否点击的是面包屑
                if(delValueId != null){
                    boolean bool = valueId.equals(delValueId);
                    if(!bool) {
                        urlParam = urlParam + "&valueId=" + valueId;
                    }
                }else {
                    urlParam = urlParam + "&valueId=" + valueId;
                }
            }
        }
        return  urlParam;
    }

}
