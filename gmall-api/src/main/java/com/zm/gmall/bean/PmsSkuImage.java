package com.zm.gmall.bean;

import org.springframework.web.bind.annotation.CrossOrigin;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class PmsSkuImage implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String skuId;
    @Column
    private String imgName;
    @Column
    private String imgUrl;
    @Column
    private String productImgId;
    @Column
    private String isDefault;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getProductImgId() {
        return productImgId;
    }

    public void setProductImgId(String productImgId) {
        this.productImgId = productImgId;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
}