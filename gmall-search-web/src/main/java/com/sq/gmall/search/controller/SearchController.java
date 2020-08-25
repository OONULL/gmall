  package com.sq.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.annotations.LoginRequired;
import com.sq.gmall.bean.*;
import com.sq.gmall.service.manage.AttrService;
import com.sq.gmall.service.search.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * @title: SearchController
 * @Description
 * @Author sq
 * @Date: 2020/8/6 20:37
 * @Version 1.0
 */
@Controller
public class SearchController {

    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;

    /**
     * 根据条件搜索商品并跳转搜索界面
     * @param pmsSearchParam
     * @param modelMap
     * @return
     */
    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){
        //调用搜索服务,返回结果
        List<PmsSearchSkuInfo> searchSkuInfoList = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",searchSkuInfoList);

        //抽取检索结果包含的平台属性集合
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : searchSkuInfoList) {
            for (PmsSkuAttrValue pmsSkuAttrValue : pmsSearchSkuInfo.getSkuAttrValueList()) {
                valueIdSet.add(pmsSkuAttrValue.getValueId());
            }
        }
        //根据valueId查询属性列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.getAttrValueListByBalueId(valueIdSet);
        modelMap.put("attrList",pmsBaseAttrInfoList);

        //对平台属性进行去除选中(迭代器删除)和面包屑url删除参数
        String[] delValueIds = pmsSearchParam.getValueId();
        if(delValueIds!=null && delValueIds.length>0){
            //面包屑
            List<PmsSearchCrumbs> pmsSearchCrumbsList = new ArrayList<>();
            for (String delValueId : delValueIds) {
                // 生成面包屑的参数
                PmsSearchCrumbs pmsSearchCrumb = new PmsSearchCrumbs();
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, delValueId));

                //迭代平台属性集合
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            // 查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在属性
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbsList.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList",pmsSearchCrumbsList);
        }

        //面包屑跳转url
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);


        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",keyword);
        }

        return "list";
    }

    /**
     * 面包屑跳转url
     * @param pmsSearchParam
     * @return
     */
    private String getUrlParam(PmsSearchParam pmsSearchParam,String...delValueIds) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] valueIds = pmsSearchParam.getValueId();
        StringBuffer stringBuffer = new StringBuffer();
        if(StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(stringBuffer)){
                stringBuffer.append("&");
            }
            stringBuffer.append("keyword=").append(keyword);
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(stringBuffer)){
                stringBuffer.append("&");
            }
            stringBuffer.append("catalog3Id=").append(catalog3Id);
        }
        if(valueIds!=null&&valueIds.length>0) {
            for (String s : pmsSearchParam.getValueId()) {
                if(delValueIds !=null && delValueIds.length>0) {
                    for (String delValueId : delValueIds) {
                        if (!s.equals(delValueId)) {
                            stringBuffer.append("&valueId=").append(s);
                        }
                    }
                }else {
                    stringBuffer.append("&valueId=").append(s);
                }
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 跳转首页
     * @return
     */
    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){

        return "index";
    }
}
