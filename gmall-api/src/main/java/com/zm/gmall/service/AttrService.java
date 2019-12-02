package com.zm.gmall.service;

import com.zm.gmall.bean.PmsBaseAttrInfo;
import com.zm.gmall.bean.PmsBaseAttrValue;
import com.zm.gmall.bean.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface AttrService {
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    public List<PmsBaseAttrValue> getAttrValueList(String attrId);

    public List<PmsBaseSaleAttr> baseSaleAttrList();

    public List<PmsBaseAttrInfo> getAttrValueByValueId(Set<String> valueIdSet);
}
