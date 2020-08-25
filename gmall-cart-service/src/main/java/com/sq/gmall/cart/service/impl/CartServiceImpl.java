package com.sq.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.sq.gmall.bean.OmsCartItem;
import com.sq.gmall.cart.mapper.OmsCartItemMapper;
import com.sq.gmall.service.cart.CartService;
import com.sq.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * @title: CartServiceImpl
 * @Description
 * @Author sq
 * @Date: 2020/8/9 19:14
 * @Version 1.0
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 根据用户查询购物车
     * @param memberId
     * @return
     */
    @Override
    public List<OmsCartItem> getCartsByUser(String memberId) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",memberId);
        return omsCartItemMapper.selectByExample(example);
    }

    /**
     * 根据用户id和skuId查询购物车商品
     * @param memberId
     * @param skuId
     * @return
     */
    @Override
    public OmsCartItem getCartByUserAndSkuId(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        return omsCartItemMapper.selectOne(omsCartItem);
    }

    /**
     * 更新购物车
     * @param cartItem
     */
    @Override
    public void update(OmsCartItem cartItem) {
        if(StringUtils.isNotBlank(cartItem.getMemberId())){
            omsCartItemMapper.updateByPrimaryKeySelective(cartItem);
        }

    }

    /**
     * 添加购物车
     * @param omsCartItem
     */
    @Override
    public void add(OmsCartItem omsCartItem) {
        if(StringUtils.isNotBlank(omsCartItem.getMemberId())) {
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    /**
     * 同步缓存中
     * @param memberId
     */
    @Override
    public void flushCartCache(String memberId) {
        List<OmsCartItem> cartItemList = getCartsByUser(memberId);
        Map<String,String> map = new HashMap<>();
        for (OmsCartItem omsCartItem : cartItemList) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            map.put(omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
        }
        //同步到redis缓存
        Jedis jedis = redisUtil.getJedis();
        //删除缓存
        jedis.del("user:"+memberId+":cart");
        //添加缓存
        jedis.hmset("user:"+memberId+":cart",map);

        jedis.close();

    }

    /**
     * 获取购物车列表信息
     * @param userId
     * @return
     */
    @Override
    public List<OmsCartItem> cartList(String userId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        try {
            //同步到redis缓存
            jedis = redisUtil.getJedis();
            Map<String, String> all = jedis.hgetAll("user:" + userId + ":cart");
            Set<String> hkeys = all.keySet();
            TreeSet<String> treeSet = new TreeSet<>();
            treeSet.addAll(hkeys);
            System.out.println(treeSet);
            for (String hkey : treeSet) {
                OmsCartItem omsCartItem = JSON.parseObject(all.get(hkey), OmsCartItem.class);
                omsCartItemList.add(omsCartItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }

        return omsCartItemList;
    }

    /**
     * 修改商品状态
     * @param omsCartItem
     */
    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("memberId",omsCartItem.getMemberId());
        criteria.andEqualTo("productSkuId",omsCartItem.getProductSkuId());

        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);

        //缓存同步
        flushCartCache(omsCartItem.getMemberId());
    }

    /**
     * 删除购物车商品
     * @param productSkuId
     */
    @Override
    public void delCart(String productSkuId,String memberId) {
        Example example = new Example(OmsCartItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("memberId",memberId);
        criteria.andEqualTo("productSkuId",productSkuId);
        omsCartItemMapper.deleteByExample(example);

        //缓存同步
        flushCartCache(memberId);
    }
}
