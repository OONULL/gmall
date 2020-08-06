package com.sq.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.sq.gmall.bean.PmsSkuAttrValue;
import com.sq.gmall.bean.PmsSkuImage;
import com.sq.gmall.bean.PmsSkuInfo;
import com.sq.gmall.bean.PmsSkuSaleAttrValue;
import com.sq.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.sq.gmall.manage.mapper.PmsSkuImageMapper;
import com.sq.gmall.manage.mapper.PmsSkuInfoMapper;
import com.sq.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.sq.gmall.service.manage.SkuService;
import com.sq.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

/**
 * @title: SkuServiceImpl
 * @Description
 * @Author sq
 * @Date: 2020/7/31 20:43
 * @Version 1.0
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 添加sku
     *
     * @param pmsSkuInfo
     * @return
     */
    @Override
    @Transactional
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        try {
            int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
            if (i > 0) {
                String skuInfoId = pmsSkuInfo.getId();
                //删除sku图片
                PmsSkuImage pmsSkuImage1 = new PmsSkuImage();
                pmsSkuImage1.setSkuId(skuInfoId);
                pmsSkuImageMapper.delete(pmsSkuImage1);
                //删除sku平台属性
                PmsSkuAttrValue pmsSkuAttrValue1 = new PmsSkuAttrValue();
                pmsSkuAttrValue1.setSkuId(skuInfoId);
                pmsSkuAttrValueMapper.delete(pmsSkuAttrValue1);
                //删除sku销售属性值
                PmsSkuSaleAttrValue pmsSkuSaleAttrValue1 = new PmsSkuSaleAttrValue();
                pmsSkuSaleAttrValue1.setSkuId(skuInfoId);
                pmsSkuSaleAttrValueMapper.delete(pmsSkuSaleAttrValue1);
                //添加sku图片
                for (PmsSkuImage pmsSkuImage : pmsSkuInfo.getSkuImageList()) {
                    pmsSkuImage.setSkuId(skuInfoId);
                    pmsSkuImageMapper.insertSelective(pmsSkuImage);
                }
                //添加sku平台属性
                for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuInfo.getSkuAttrValueList()) {
                    pmsSkuAttrValue.setSkuId(skuInfoId);
                    pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
                }
                //添加sku销售属性值
                for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuInfo.getSkuSaleAttrValueList()) {
                    pmsSkuSaleAttrValue.setSkuId(skuInfoId);
                    pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
                }
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "error";
    }

    /**
     * 修改sku
     *
     * @param pmsSkuInfo
     * @return
     */
    @Override
    public String updateSkuInfo(PmsSkuInfo pmsSkuInfo) {
        if (!StringUtils.isEmpty(pmsSkuInfo.getId()) && !StringUtils.isEmpty(pmsSkuInfoMapper.selectByPrimaryKey(pmsSkuInfo.getId()))) {
            try {
                int i = pmsSkuInfoMapper.updateByPrimaryKeySelective(pmsSkuInfo);
                if (i > 0) {
                    String skuInfoId = pmsSkuInfo.getId();
                    //删除sku图片
                    PmsSkuImage pmsSkuImage1 = new PmsSkuImage();
                    pmsSkuImage1.setSkuId(skuInfoId);
                    pmsSkuImageMapper.delete(pmsSkuImage1);
                    //删除sku平台属性
                    PmsSkuAttrValue pmsSkuAttrValue1 = new PmsSkuAttrValue();
                    pmsSkuAttrValue1.setSkuId(skuInfoId);
                    pmsSkuAttrValueMapper.delete(pmsSkuAttrValue1);
                    //删除sku销售属性值
                    PmsSkuSaleAttrValue pmsSkuSaleAttrValue1 = new PmsSkuSaleAttrValue();
                    pmsSkuSaleAttrValue1.setSkuId(skuInfoId);
                    pmsSkuSaleAttrValueMapper.delete(pmsSkuSaleAttrValue1);
                    //添加sku图片
                    for (PmsSkuImage pmsSkuImage : pmsSkuInfo.getSkuImageList()) {
                        pmsSkuImage.setSkuId(skuInfoId);
                        pmsSkuImageMapper.insertSelective(pmsSkuImage);
                    }
                    //添加sku平台属性
                    for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuInfo.getSkuAttrValueList()) {
                        pmsSkuAttrValue.setSkuId(skuInfoId);
                        pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
                    }
                    //添加sku销售属性值
                    for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuInfo.getSkuSaleAttrValueList()) {
                        pmsSkuSaleAttrValue.setSkuId(skuInfoId);
                        pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
                    }
                    return "success";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "error";
    }

    /**
     * 根据skuid获取商品详情页数据
     *
     * @param skuId
     * @return
     */
    @Override
    public PmsSkuInfo getSkuById(String skuId) {

        //redis连接缓存
        Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuInfoJson = jedis.get(skuKey);
        PmsSkuInfo skuInfo;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(skuInfoJson)) {
            skuInfo = JSON.parseObject(skuInfoJson, PmsSkuInfo.class);
        } else {
            //缓存中没有查询mysql
            //设置分布式锁
            String uuid = UUID.randomUUID().toString();
            String ok = jedis.set("sku:" + skuId + ":lock", uuid, "nx", "px", 10000);//过期时间10秒
            if (org.apache.commons.lang3.StringUtils.isNotBlank(ok) && "ok".equals(ok)) {
                //获取锁成功(10内访问数据库获取数据)
                skuInfo = getSkuByIdFromDb(skuId);
                if (!StringUtils.isEmpty(skuInfo)) {
                    String jsonString = JSON.toJSONString(skuInfo);
                    //mysql查询结果存入redis
                    jedis.set("sku:" + skuInfo.getId() + ":info", jsonString);
                } else {
                    //防止缓存穿透
                    jedis.setex("sku:" + skuInfo.getId() + ":info", 60 * 3, null);
                }
                //访问完成后将分布式锁释放
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                //判断是否是自己的锁
                if(org.apache.commons.lang3.StringUtils.isNotBlank(lockToken)&&uuid.equals(lockToken)){
                    //jedis.eval("lua");可与用lua脚本，在查询到key的同时删除该key，防止高并发下的意外的发生
                    jedis.del("sku:" + skuId + ":lock");
                }

            } else {
                //获取锁失败,自旋(该线程睡眠几秒后,重新尝试访问该方法)
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }
        }
        //关闭redis
        jedis.close();
        return skuInfo;
    }

    /**
     * 查询当前sku的spu的其他sku集合
     *
     * @param productId
     * @return
     */
    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        return pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);

    }

    /**
     * 获取sku
     *
     * @param skuId
     * @return
     */
    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        // sku商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        // sku的图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    /**
     * 根据3级类目id获取所有sku与sku平台属性数据
     * @return
     */
    @Override
    public List<PmsSkuInfo> getAllSku(){
        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo skuInfo : pmsSkuInfoList) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuInfo.getId());
            skuInfo.setSkuAttrValueList(pmsSkuAttrValueMapper.select(pmsSkuAttrValue));
        }
        return pmsSkuInfoList;
    }
}
