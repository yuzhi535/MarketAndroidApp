package com.demo.market.models;


/**
 * 商品类型Vo
 */

public class CommodityTypeModel {
    /**
     * 商品类别自增id
     */
    private Long id;

    /**
     * 商品类别解释
     */
    private String name;

    /**
     * 图标地址
     */
    private String iconPath;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}
