package com.sq.gmall.service.manage;

import com.sq.gmall.bean.PmsProductImage;
import com.sq.gmall.bean.PmsProductInfo;
import com.sq.gmall.bean.PmsProductSaleAttr;

import java.util.List;

/**
 * @Description spu业务接口
 * @Author sq
 * Created by sq on 2020/7/29 21:11
 */
public interface SpuService {
    /**
     * 根据3级类目id获取spu列表
     * @param catalog3Id
     * @return
     */
    List<PmsProductInfo> spuList(String catalog3Id);

    /**
     * 添加spu(商品)
     * @param pmsProductInfo
     * @return
     */
    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    /**
     * 根据id删除spu
     * @param id
     * @return
     */
    String deleteSpuInfo(String id);

    /**
     * 修改spu(商品)
     * @param pmsProductInfo
     * @return
     */
    String updateSpuInfo(PmsProductInfo pmsProductInfo);

    /**
     * 根据id查询销售属性值
     * @param spuId
     * @return
     */
    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    /**
     * 根据id查询spu图片列表
     * @param spuId
     * @return
     */
    List<PmsProductImage> spuImageList(String spuId);

    /**
     * 根据productId获取销售属性列表
     * @param productId
     * @return
     */
    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);
}
