package com.sq.gmall.service.manage;

import com.sq.gmall.bean.PmsBaseAttrInfo;
import com.sq.gmall.bean.PmsBaseAttrValue;
import com.sq.gmall.bean.PmsBaseSaleAttr;

import java.util.List;

/**
 * @Description
 * @Author sq
 * Created by sq on 2020/7/28 21:44
 */
public interface AttrService {
    /**
     * 根据3级类目获取类目属性
     * @param catalog3Id
     * @return
     */
    List<PmsBaseAttrInfo> attrInfoList(Integer catalog3Id);

    /**
     * 添加类目属性
     * @param pmsBaseAttrInfo
     * @return
     */
    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    /**
     * 根据id删除类目属性
     * @param id
     * @return
     */
    String deleteAttrInfo(String id);


    /**
     * 根据类目属性id获取类目属性的值
     * @param attrId
     * @return
     */
    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    /**
     * 获取商家销售属性
     * @return
     */
    List<PmsBaseSaleAttr> baseSaleAttrList();
}
