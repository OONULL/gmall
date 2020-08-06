package com.sq.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.bean.PmsProductImage;
import com.sq.gmall.bean.PmsProductInfo;
import com.sq.gmall.bean.PmsProductSaleAttr;
import com.sq.gmall.manage.util.PmsUploadUtil;
import com.sq.gmall.service.manage.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @title: SpuController
 * @Description
 * @Author sq
 * @Date: 2020/7/29 21:00
 * @Version 1.0
 */
@Controller
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;

    /**
     * 根据3级类目id获取spu列表
     * @param catalog3Id
     * @return
     */
    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        return spuService.spuList(catalog3Id);
    }

    /**
     * 添加spu(商品)
     * @param pmsProductInfo
     * @return
     */
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
            return spuService.saveSpuInfo(pmsProductInfo);
    }

    /**
     * 上传图片或视频
     * @param multipartFile
     * @return
     */
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        return PmsUploadUtil.uploadImage(multipartFile);

    }

    /**
     * 根据id查询销售属性值列表
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        return spuService.spuSaleAttrList(spuId);
    }

    /**
     * 根据id查询spu图片列表
     * @return
     */
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){
        return spuService.spuImageList(spuId);
    }
}
