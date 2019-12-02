package com.zm.gmall.service;

import com.zm.gmall.bean.PmsBrand;
import com.zm.gmall.bean.PmsSearchParam;
import com.zm.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchServcie {
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);

    public List<PmsBrand> getPmsBrandList();
}
