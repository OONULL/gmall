package com.sq.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.sq.gmall.bean.PmsProductImage;
import com.sq.gmall.bean.PmsProductInfo;
import com.sq.gmall.bean.PmsProductSaleAttr;
import com.sq.gmall.bean.PmsProductSaleAttrValue;
import com.sq.gmall.manage.mapper.PmsProductImageMapper;
import com.sq.gmall.manage.mapper.PmsProductInfoMapper;
import com.sq.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.sq.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.sq.gmall.service.manage.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @title: SpuServiceImpl
 * @Description spu业务接口实现类
 * @Author sq
 * @Date: 2020/7/29 21:10
 * @Version 1.0
 */
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    /**
     * 根据3级类目id获取spu列表
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        return pmsProductInfoMapper.select(pmsProductInfo);
    }

    /**
     * 添加spu(商品)
     * @param pmsProductInfo
     * @return
     */
    @Override
    @Transactional
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        try {
            //保存spu
            int i = pmsProductInfoMapper.insertSelective(pmsProductInfo);
            //获取spuid
            String pmsProductInfoId = pmsProductInfo.getId();
            if(i>0){
                //删除之前spu图片
                PmsProductImage pmsProductImage1 = new PmsProductImage();
                pmsProductImage1.setProductId(pmsProductInfoId);
                pmsProductImageMapper.delete(pmsProductImage1);
                //删除之前销售属性信息
                PmsProductSaleAttr pmsProductSaleAttr1 = new PmsProductSaleAttr();
                pmsProductSaleAttr1.setProductId(pmsProductInfoId);
                pmsProductSaleAttrMapper.delete(pmsProductSaleAttr1);
                //删除之前销售属性值
                PmsProductSaleAttrValue pmsProductSaleAttrValue1 = new PmsProductSaleAttrValue();
                pmsProductSaleAttrValue1.setProductId(pmsProductInfoId);
                pmsProductSaleAttrValueMapper.delete(pmsProductSaleAttrValue1);
                //遍历保存spu图片
                for (PmsProductImage pmsProductImage : pmsProductInfo.getSpuImageList()) {
                    pmsProductImage.setProductId(pmsProductInfoId);
                    pmsProductImageMapper.insertSelective(pmsProductImage);
                }
                // 保存销售属性信息
                for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductInfo.getSpuSaleAttrList()) {
                    pmsProductSaleAttr.setProductId(pmsProductInfoId);
                    pmsProductSaleAttrMapper.insert(pmsProductSaleAttr);
                    // 保存销售属性值
                    for (PmsProductSaleAttrValue pmsProductSaleAttrValue : pmsProductSaleAttr.getSpuSaleAttrValueList()) {
                        pmsProductSaleAttrValue.setProductId(pmsProductInfoId);
                        pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr.getId());
                        pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
                    }
                }
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "error";
    }

    /**
     * 根据id删除spu
     * @param id
     * @return
     */
    @Override
    @Transactional
    public String deleteSpuInfo(String id){
        try {
            PmsProductInfo pmsProductInfo = pmsProductInfoMapper.selectByPrimaryKey(id);
            if(!StringUtils.isEmpty(pmsProductInfo)){
                //删除spu图片
                PmsProductImage pmsProductImage = new PmsProductImage();
                pmsProductImage.setProductId(id);
                pmsProductImageMapper.delete(pmsProductImage);
                //删除销售属性信息
                PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
                pmsProductSaleAttr.setProductId(id);
                pmsProductSaleAttrMapper.delete(pmsProductSaleAttr);
                //删除销售属性值
                PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
                pmsProductSaleAttrValue.setProductId(id);
                pmsProductSaleAttrValueMapper.delete(pmsProductSaleAttrValue);
                //删除spu
                pmsProductInfoMapper.deleteByPrimaryKey(id);
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }
    /**
     * 修改spu(商品)
     * @param pmsProductInfo
     * @return
     */
    @Override
    @Transactional
    public String updateSpuInfo(PmsProductInfo pmsProductInfo) {
        try {
            if(!StringUtils.isEmpty(pmsProductInfo.getId())&&!StringUtils.isEmpty(pmsProductInfoMapper.selectByPrimaryKey(pmsProductInfo.getId()))){
            //保存spu
            int i = pmsProductInfoMapper.updateByPrimaryKeySelective(pmsProductInfo);
            //获取spuid
            String pmsProductInfoId = pmsProductInfo.getId();
            if(i>0){
                //删除之前spu图片
                PmsProductImage pmsProductImage1 = new PmsProductImage();
                pmsProductImage1.setProductId(pmsProductInfoId);
                pmsProductImageMapper.delete(pmsProductImage1);
                //删除之前销售属性信息
                PmsProductSaleAttr pmsProductSaleAttr1 = new PmsProductSaleAttr();
                pmsProductSaleAttr1.setProductId(pmsProductInfoId);
                pmsProductSaleAttrMapper.delete(pmsProductSaleAttr1);
                //删除之前销售属性值
                PmsProductSaleAttrValue pmsProductSaleAttrValue1 = new PmsProductSaleAttrValue();
                pmsProductSaleAttrValue1.setProductId(pmsProductInfoId);
                pmsProductSaleAttrValueMapper.delete(pmsProductSaleAttrValue1);
                //遍历保存spu图片
                for (PmsProductImage pmsProductImage : pmsProductInfo.getSpuImageList()) {
                    pmsProductImage.setProductId(pmsProductInfoId);
                    pmsProductImageMapper.insertSelective(pmsProductImage);
                }
                // 保存销售属性信息
                for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductInfo.getSpuSaleAttrList()) {
                    pmsProductSaleAttr.setProductId(pmsProductInfoId);
                    pmsProductSaleAttrMapper.insert(pmsProductSaleAttr);
                    // 保存销售属性值
                    for (PmsProductSaleAttrValue pmsProductSaleAttrValue : pmsProductSaleAttr.getSpuSaleAttrValueList()) {
                        pmsProductSaleAttrValue.setProductId(pmsProductInfoId);
                        pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr.getId());
                        pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
                    }
                }
                return "success";
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "error";
    }

    /**
     * 根据id查询销售属性和销售属性值
     * @param spuId
     * @return
     */
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
       //遍历获取销售属性值
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrList) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValueList);
        }
        return pmsProductSaleAttrList;
    }

    /**
     * 根据id查询spu图片列表
     * @param spuId
     * @return
     */
    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        return pmsProductImageMapper.select(pmsProductImage);
    }

    /**
     * 根据productId获取销售属性列表
     * @param productId
     * @return
     */
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {
        /*//获取销售属性列表
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(productId);
        List<PmsProductSaleAttr> saleAttrList = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        //遍历获取销售属性值
        for (PmsProductSaleAttr productSaleAttr : saleAttrList) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            pmsProductSaleAttrValue.setProductId(productId);
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValueList);
        }*/

        List<PmsProductSaleAttr> saleAttrList = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);
        return saleAttrList;
    }
}
