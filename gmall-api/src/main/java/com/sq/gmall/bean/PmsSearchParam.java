package com.sq.gmall.bean;

import java.io.Serializable;

/**
 * @title: PmsSearchParam
 * @Description
 * @Author sq
 * @Date: 2020/8/6 21:01
 * @Version 1.0
 */
public class PmsSearchParam implements Serializable {

    private String catalog3Id;
    private String keyword;
    private String[] valueId;


    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String[] getValueId() {
        return valueId;
    }

    public void setValueId(String[] valueId) {
        this.valueId = valueId;
    }
}

