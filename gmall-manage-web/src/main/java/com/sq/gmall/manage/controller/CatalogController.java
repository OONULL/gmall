package com.sq.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.bean.PmsBaseCatalog1;
import com.sq.gmall.bean.PmsBaseCatalog2;
import com.sq.gmall.bean.PmsBaseCatalog3;
import com.sq.gmall.service.manage.CatelogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @title: CatalogController
 * @Description
 * @Author sq
 * @Date: 2020/7/28 19:44
 * @Version 1.0
 */
@Controller
@CrossOrigin
public class CatalogController {
    @Reference
    private CatelogService catelogService;
    /**
     * 获取商品1级类目
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        return catelogService.getCatalog1();

    }
    /**
     * 获取商品2级类目
     * @return
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2( Integer catalog1Id){
        return catelogService.getCatalog2(catalog1Id);

    }
    /**
     * 获取商品3级类目
     * @return
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(Integer catalog2Id){
        return catelogService.getCatalog3(catalog2Id);

    }
}
