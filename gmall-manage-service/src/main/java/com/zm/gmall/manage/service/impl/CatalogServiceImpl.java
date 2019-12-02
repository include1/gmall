package com.zm.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zm.gmall.bean.PmsBaseCatalog1;
import com.zm.gmall.bean.PmsBaseCatalog2;
import com.zm.gmall.bean.PmsBaseCatalog3;
import com.zm.gmall.manage.dao.Catalog2Mapper;
import com.zm.gmall.manage.dao.Catalog3Mapper;
import com.zm.gmall.manage.dao.Catalog1Mapper;
import com.zm.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class CatalogServiceImpl implements CatalogService {
    @Autowired
    Catalog1Mapper catalog1Mapper;
    @Autowired
    Catalog2Mapper catalog2Mapper;
    @Autowired
    Catalog3Mapper catalog3Mapper;
    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return catalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        PmsBaseCatalog2 catalog2 = new PmsBaseCatalog2();
        catalog2.setCatalog1Id(catalog1Id);
        return catalog2Mapper.select(catalog2);
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        PmsBaseCatalog3 catalog3 = new PmsBaseCatalog3();
        catalog3.setCatalog2Id(catalog2Id);
        return catalog3Mapper.select(catalog3);
    }
}
