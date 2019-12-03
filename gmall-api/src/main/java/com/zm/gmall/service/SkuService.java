package com.zm.gmall.service;

        import com.zm.gmall.bean.PmsSkuInfo;

        import java.util.List;

public interface SkuService {

    public String saveSkuInfo( PmsSkuInfo pmsSkuInfo);

    public PmsSkuInfo getSkuInfoById(String skuId);

    public List<PmsSkuInfo> getSkuInfoListCheckBySkuId(String spuId);

    public PmsSkuInfo getSkuInfoByIdFromRedis(String skuId,String ip);

    public List<PmsSkuInfo> getSkuInfoListCheckBySkuIdFromRedis(String spuId);

    public List<PmsSkuInfo> getAllSkuInfo();
}
