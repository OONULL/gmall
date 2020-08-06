package com.sq.gmall.manage.mapper;


import com.sq.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 销售属性信息
 */
public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr>{

    List<PmsProductSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("productId") String productId,@Param("skuId") String skuId);
}
