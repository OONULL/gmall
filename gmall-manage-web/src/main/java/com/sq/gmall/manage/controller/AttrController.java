package com.sq.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.bean.PmsBaseAttrInfo;
import com.sq.gmall.bean.PmsBaseAttrValue;
import com.sq.gmall.bean.PmsBaseSaleAttr;
import com.sq.gmall.service.manage.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @title: AttrController
 * @Description 类目平台属性控制层
 * @Author sq
 * @Date: 2020/7/28 21:39
 * @Version 1.0
 */
@Controller
@CrossOrigin
public class AttrController {

    @Reference
    private AttrService attrService;

    /**
     * 获取商家销售属性
     * @return
     */
    @RequestMapping(value = "baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        return attrService.baseSaleAttrList();
    }


    /**
     * 根据3级类目获取类目平台属性
     * @param catalog3Id
     * @return
     */
    @RequestMapping(value = "attrInfoList",method = RequestMethod.GET)
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(Integer catalog3Id){

        return  attrService.attrInfoList(catalog3Id);
    }

    /**
     * 添加/更新类目平台属性
     * @param pmsBaseAttrInfo
     * @return
     */
    @RequestMapping(value = "saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        return attrService.saveAttrInfo(pmsBaseAttrInfo);
    }


    /**
     * 根据id删除类目平台属性
     * @param id
     * @return
     */
    @RequestMapping(value = "deleteAttrInfo")
    @ResponseBody
    public String deleteAttrInfo(String id){
        return attrService.deleteAttrInfo(id);
    }

    /**
     * 根据类目属性id获取类目平台属性的值
     * @param attrId
     * @return
     */
    @RequestMapping(value = "getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){
        return attrService.getAttrValueList(attrId);
    }




}
