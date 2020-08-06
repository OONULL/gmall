package com.sq.gmall.manage.mapper;


import com.sq.gmall.bean.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo>{
    /**
     * 根据spuId查询当前sku的spu的其他sku集合
     * @param productId
     * @return
     */
    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(String productId);
}
