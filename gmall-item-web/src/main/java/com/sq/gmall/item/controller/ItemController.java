package com.sq.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.sq.gmall.bean.PmsProductSaleAttr;
import com.sq.gmall.bean.PmsSkuInfo;
import com.sq.gmall.bean.PmsSkuSaleAttrValue;
import com.sq.gmall.service.manage.SkuService;
import com.sq.gmall.service.manage.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: ItemController
 * @Description
 * @Author sq
 * @Date: 2020/8/1 16:53
 * @Version 1.0
 */
@Controller
public class ItemController {
    @Reference
    private SkuService skuService;
    @Reference
    private SpuService spuService;

    /*@RequestMapping("index")
    public String index(ModelMap modelMap){

        modelMap.put("wwk", "wswwk");
        return "index";
    }*/

    /**
     * 根据skuid获取商品详情页数据
     *
     * @param skuId
     * @param map
     * @return
     */
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map) {
        //sku对象
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        map.put("skuInfo", skuInfo);
        //销售属性列表
        List<PmsProductSaleAttr> spuSaleAttrList = spuService.spuSaleAttrListCheckBySku(skuInfo.getProductId(),skuId);
        map.put("spuSaleAttrListCheckBySku", spuSaleAttrList);

        //查询当前sku的spu的其他sku集合的hash表
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getSkuSaleAttrValueListBySpu(skuInfo.getProductId());
        Map<String,String> skuSaleAttrHash = new HashMap<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            String key = "";
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuInfo.getSkuSaleAttrValueList()) {
                key += pmsSkuSaleAttrValue.getSaleAttrValueId()+"|";
            }
            skuSaleAttrHash.put(key,pmsSkuInfo.getId());
        }
        //转化为json字符串
        String jsonString = JSON.toJSONString(skuSaleAttrHash);
        //将
        map.put("skuSaleAttrHashJsonStr",jsonString);
        return "item";
    }


}
