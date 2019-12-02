package com.zm.gmall.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
//该类必须进行序列化，才能进行网络传输
public class PmsBaseCatalog3 implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String name;
    @Column
    private String catalog2Id;
    @Transient
    private List<PmsBaseCatalog1> baseCatalog1s;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PmsBaseCatalog1> getBaseCatalog1s() {
        return baseCatalog1s;
    }

    public String getCatalog2Id() {
        return catalog2Id;
    }

    public void setCatalog2Id(String catalog2Id) {
        this.catalog2Id = catalog2Id;
    }

    public void setBaseCatalog1s(List<PmsBaseCatalog1> baseCatalog1s) {
        this.baseCatalog1s = baseCatalog1s;
    }
}
