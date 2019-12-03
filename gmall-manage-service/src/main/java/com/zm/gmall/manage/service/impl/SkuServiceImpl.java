package com.zm.gmall.manage.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.zm.gmall.bean.*;
import com.zm.gmall.manage.dao.*;
import com.zm.gmall.service.SkuService;
import com.zm.gmall.util.RedisUtil;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //首先判断默认图片是否为空
        String defaultImg = pmsSkuInfo.getSkuDefaultImg();
        if(StringUtils.isBlank(defaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
        //首先添加skuinfo表中的数据
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        //向skuimageList表中插入数据
        List<PmsSkuImage> pmsSkuImageList = pmsSkuInfo.getSkuImageList();
        for(PmsSkuImage pmsSkuImage : pmsSkuImageList){
            //查询商品中图片id,并插入skuimage表中
            PmsProductImage pmsProductImage = new PmsProductImage();
            pmsProductImage.setProductId(pmsSkuInfo.getSpuId());
            pmsProductImage.setImgName(pmsSkuImage.getImgName());
            PmsProductImage productImage = pmsProductImageMapper.selectOne(pmsProductImage);
            //设置商品和库存id
            pmsSkuImage.setProductImgId(productImage.getId());
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
        //向skuattrValue表中插入数据
        List<PmsSkuAttrValue> pmsSkuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for(PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValueList){
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //向skuSaleAttrValue表中，插入数据
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for(PmsSkuSaleAttrValue  pmsSkuSaleAttrValue : pmsSkuSaleAttrValueList){
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        return "success";
    }

    /**
     * @Desc 直接查询数据库
      * @param skuId
     * @return PmsSkuInfo
     */
    @Override
    public PmsSkuInfo getSkuInfoById(String skuId) {
        //使用redis缓存数据，提高查询效率
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        //获取图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImageList = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImageList);
        //获取属性值
        PmsSkuSaleAttrValue pmsSkuSaleAttrValue = new PmsSkuSaleAttrValue();
        pmsSkuSaleAttrValue.setSkuId(skuId);
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValueList = pmsSkuSaleAttrValueMapper.select(pmsSkuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(pmsSkuSaleAttrValueList);
        return skuInfo;
    }

    /**
     * @Dec s使用redis作为缓存，解决高并发问题
     * @param skuId
     * @return pmsSkuInfo
     */
    @Override
    public PmsSkuInfo getSkuInfoByIdFromRedis(String skuId,String ip) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //与redis建立连接
        Jedis jedis = redisUtil.getJedis();
        //通过key查询redis缓冲数据key(表名：表名Id:表名的字段)
        String key = "sku:"+skuId+":info";
        String str = jedis.get(key);

        if(!StringUtils.isBlank(str))
        {
            pmsSkuInfo = JSON.parseObject(str, PmsSkuInfo.class);
        } else{
            //首先设置redis分布式锁，党请求获取锁，才有权利访问数据库
            //设置一个随机数，标识每个请求的锁
            String token = UUID.randomUUID().toString();
            String lock = jedis.set("sku:"+skuId+":lock",token, "nx", "px", 1000);
            if(!StringUtils.isBlank(lock)&&lock.equals("OK")){
                //若没有查询到数据，查询mysql数据库数据
                pmsSkuInfo = getSkuInfoById(skuId);
                //查询的结果，放到redis的缓存中，并返回给请求所需的数据
                if(pmsSkuInfo != null){
                    String jsonStr = JSON.toJSONString(pmsSkuInfo);
                    jedis.append(key,jsonStr);
                }else{
                    jedis.append(key,JSON.toJSONString(""));
                }
                //释放锁，给其他请求使用
                //判断删除的是那个线程的锁，防止锁过期后，删除不是本身的锁
                String tokenValue = jedis.get("sku:" + skuId + ":lock");
                System.out.println("标识锁的值："+tokenValue);
                if(!StringUtils.isBlank(tokenValue)&&tokenValue.equals(token)) {
                    jedis.del("sku:" + skuId + ":lock");//用token确认删除sku的锁
                }
            }else{
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfoByIdFromRedis(skuId,ip);
            }
        }
        jedis.close();
        return pmsSkuInfo;
    }
    @Override
    public List<PmsSkuInfo> getSkuInfoListCheckBySkuId(String spuId) {
        return pmsSkuInfoMapper.selectSkuInfoListCheckBySkuId(spuId);
    }
    @Override
    public List<PmsSkuInfo> getSkuInfoListCheckBySkuIdFromRedis(String spuId) {
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();
        //与redis建立连接
        Jedis jedis = redisUtil.getJedis();
        //查询redis缓冲中是否有key
        String key = "sku:"+spuId+":info1";
        String value = jedis.get(key);
        if(!StringUtils.isBlank(value)){
            //如果查询到数据，把json字符串解析
            pmsSkuInfoList = JSON.parseArray(value,PmsSkuInfo.class);
        }else{
            //如果redis中，没有查询到数据，直接查询数据库
            //查询数据库必须设置一个分布式锁(这是redis自带的一个锁)，防止缓冲击穿
            //无死锁，容错，唯一性
            String token = UUID.randomUUID().toString();//标识每个请求获取锁
            String  lock = jedis.set("sku:"+spuId+":lock", token, "nx", "px", 1000);
            if(!StringUtils.isBlank(lock)&&lock.equals("OK")) {
                //获取了锁
                //查询数据库
                pmsSkuInfoList = getSkuInfoListCheckBySkuId(spuId);
                //判断查询是否为空
                if (pmsSkuInfoList != null) {
                    jedis.append(key, JSON.toJSONString(pmsSkuInfoList));
                } else {
                    jedis.append(key, JSON.toJSONString(""));
                }
                //使用完锁进行释放
                String s = jedis.get("sku:" + spuId + ":lock");
                if(!StringUtils.isBlank(s)&&s.equals(token)){
                    //假如分部锁，再判断的时候失效，无法删除之前的锁.,可以是lua脚本删除，防止在判断时，锁过期，没有删除指定的锁
                    jedis.del("sku:" + spuId + ":lock");
                }
            }else{
                //如果没有获取锁，自旋操作
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfoListCheckBySkuIdFromRedis(spuId);
            }

        }
        jedis.close();
        return pmsSkuInfoList;
    }

    /**
     * @DEsc 获取全部库存商品，放入elasticsarch中
     * @Author:zm
     * @return
     */
    @Override
    public List<PmsSkuInfo> getAllSkuInfo() {
        //PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //pmsSkuInfo.setCatalog3Id(catalog3Id);
        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectAll();
        //库存属性列表数据
        for(PmsSkuInfo pmsSkuInfo1 : pmsSkuInfoList){
            String id = pmsSkuInfo1.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(id);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo1.setSkuAttrValueList(select);
        }
        return pmsSkuInfoList;
    }
}
