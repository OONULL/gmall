package com.sq.gmall.service.manage;

import com.sq.gmall.bean.PmsSkuInfo;

import java.util.List;

/**
 * @Description
 * @Author sq
 * Created by sq on 2020/7/31 20:42
 */
public interface SkuService {
    /**
     * 添加sku
     * @param pmsSkuInfo
     * @return
     */
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    /**
     * 修改sku
     * @param pmsSkuInfo
     * @return
     */
    String updateSkuInfo(PmsSkuInfo pmsSkuInfo);

    /**
     * 根据skuid获取商品详情页数据
     * @param skuId
     * @return
     */
    PmsSkuInfo getSkuById(String skuId);

    /**
     * 查询当前sku的spu的其他sku集合的hash表
     * @param productId
     * @return
     */
    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    /**
     * 获取所有sku与sku平台属性数据
     * @return
     */
    public List<PmsSkuInfo> getAllSku();
}
