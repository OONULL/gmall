package com.sq.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.bean.PmsSkuInfo;
import com.sq.gmall.service.manage.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @title: SkuController
 * @Description
 * @Author sq
 * @Date: 2020/7/31 21:03
 * @Version 1.0
 */
@Controller
@CrossOrigin
public class SkuController {
    @Reference
    private SkuService skuService;

    /**
     * 添加sku
     * @param pmsSkuInfo
     * @return
     */
    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        //处理默认图片
        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if(StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
        return skuService.saveSkuInfo(pmsSkuInfo);
    }
    /**
     * 修改sku
     * @param pmsSkuInfo
     * @return
     */
    @RequestMapping("updateSkuInfo")
    @ResponseBody
    public String updateSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        return skuService.updateSkuInfo(pmsSkuInfo);
    }
}
