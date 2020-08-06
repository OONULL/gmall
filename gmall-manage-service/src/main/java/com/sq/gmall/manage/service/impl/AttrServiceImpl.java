package com.sq.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.sq.gmall.bean.PmsBaseAttrInfo;
import com.sq.gmall.bean.PmsBaseAttrValue;
import com.sq.gmall.bean.PmsBaseSaleAttr;
import com.sq.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.sq.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.sq.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.sq.gmall.service.manage.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @title: AttrServiceImpl
 * @Description 类目属性业务层
 * @Author sq
 * @Date: 2020/7/28 21:45
 * @Version 1.0
 */
@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    private PmsBaseAttrInfoMapper attrInfoMapper;
    @Autowired
    private PmsBaseAttrValueMapper attrValueMapper;
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    /**
     * 根据3级类目获取类目属性和属性值
     *
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsBaseAttrInfo> attrInfoList(Integer catalog3Id) {

        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(String.valueOf(catalog3Id));
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrInfoMapper.select(pmsBaseAttrInfo);
        //遍历获取属性值
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfoList) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> baseAttrValueList = attrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(baseAttrValueList);
        }
        return pmsBaseAttrInfoList;

    }

    /**
     * 添加/更新类目属性
     *
     * @param pmsBaseAttrInfo
     * @return
     */
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        try {
            if (StringUtils.isEmpty(pmsBaseAttrInfo.getId())) {
                //不存在id则添加
                int i = attrInfoMapper.insertSelective(pmsBaseAttrInfo);
                if (i > 0) {
                    //先删除该类目属性的所有属性值
                    PmsBaseAttrValue del = new PmsBaseAttrValue();
                    del.setAttrId(pmsBaseAttrInfo.getId());
                    attrValueMapper.delete(del);
                    //遍历添加类目属性值
                    for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
                        //再添加
                        pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                        pmsBaseAttrValue.setId(null);
                        attrValueMapper.insertSelective(pmsBaseAttrValue);
                    }
                    return "success";
                }
            } else {
                //存在id则修改
                PmsBaseAttrInfo pmsBaseAttrInfo1 = attrInfoMapper.selectByPrimaryKey(pmsBaseAttrInfo.getId());
                if (!StringUtils.isEmpty(pmsBaseAttrInfo1)) {
                    int i = attrInfoMapper.updateByPrimaryKeySelective(pmsBaseAttrInfo);
                    if (i > 0) {
                        //先删除该类目属性的所有属性值
                        PmsBaseAttrValue del = new PmsBaseAttrValue();
                        del.setAttrId(pmsBaseAttrInfo.getId());
                        attrValueMapper.delete(del);
                        for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
                            //再添加
                            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                            pmsBaseAttrValue.setId(null);
                            attrValueMapper.insertSelective(pmsBaseAttrValue);
                        }
                        return "success";
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * 根据id删除类目属性
     *
     * @param id
     * @return
     */
    @Override
    public String deleteAttrInfo(String id) {
        try {
            int i = attrInfoMapper.deleteByPrimaryKey(id);
            if (i > 0) {
                PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
                pmsBaseAttrValue.setAttrId(id);
                attrValueMapper.deleteByPrimaryKey(pmsBaseAttrValue);
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }


    /**
     * 根据类目属性id获取类目属性的值
     *
     * @param attrId
     * @return
     */
    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        return attrValueMapper.select(pmsBaseAttrValue);
    }

    /**
     * 获取商家销售属性
     * @return
     */
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }
}
