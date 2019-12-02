package com.zm.gmall.manage.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.zm.gmall.bean.PmsBaseAttrInfo;
import com.zm.gmall.bean.PmsBaseAttrValue;
import com.zm.gmall.bean.PmsBaseSaleAttr;
import com.zm.gmall.manage.dao.AttrMapper;
import com.zm.gmall.manage.dao.AttrValueMapper;
import com.zm.gmall.manage.dao.PmsBaseSaleAttrMapper;
import com.zm.gmall.service.AttrService;

import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {

    //依赖注入
    @Autowired
    AttrMapper attrMapper;
    //获取所有3级目录属性列表集合
    @Autowired
    AttrValueMapper attrValueMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo attrInfo = new PmsBaseAttrInfo();
        attrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo>  pmsBaseAttrInfoList =attrMapper.select(attrInfo);
        //获取属性值
        for(PmsBaseAttrInfo pmsBaseAttrInfo : pmsBaseAttrInfoList){
            List<PmsBaseAttrValue> pmsBaseAttrValueList = new ArrayList<>();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueList = attrValueMapper.select(pmsBaseAttrValue);
            pmsBaseAttrInfo.setAttrValueList(pmsBaseAttrValueList);
        }
        return pmsBaseAttrInfoList;
    }
    /**
     * @Dec:保存属性
     * @Author:zm
     * @return string
     */
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String id = pmsBaseAttrInfo.getId();

        if(StringUtils.isBlank(id)){
            //id为空，插入

            //进行属性插入,会返回属性ID
            attrMapper.insertSelective(pmsBaseAttrInfo);
            //属性值的插入
            List<PmsBaseAttrValue> list = pmsBaseAttrInfo.getAttrValueList();
            for(PmsBaseAttrValue pmsBaseAttrValue : list) {
                //属性的Id作为属性值的外键进行插入
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                attrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }else{
            //id不为空，修改
            Example example = new Example(pmsBaseAttrInfo.getClass());
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());//创建规则,根据主键修改
            attrMapper.updateByExampleSelective(pmsBaseAttrInfo,example);//根据原始数据修改目标数据

            List<PmsBaseAttrValue> pmsBaseAttrValueList = pmsBaseAttrInfo.getAttrValueList();
            //根据属性的外键，首先删除属性值，
            PmsBaseAttrValue pmsBaseAttrValueDel = new PmsBaseAttrValue();
            pmsBaseAttrValueDel.setAttrId(pmsBaseAttrInfo.getId());
            attrValueMapper.delete(pmsBaseAttrValueDel);
            //再插入属性值
            for(PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrValueList){
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                attrValueMapper.insertSelective(pmsBaseAttrValue);
            }

        }
            return "success";
    }
    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        return attrValueMapper.select(pmsBaseAttrValue);
    }
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueByValueId(Set<String> valueIdSet) {
        String join = StringUtils.join(valueIdSet,",");

        return attrMapper.selectPmsBaseAttrInfoByValueId(join);
    }
}
