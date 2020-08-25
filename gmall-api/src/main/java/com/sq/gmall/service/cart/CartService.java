package com.sq.gmall.service.cart;

import com.sq.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * @title: CartService
 * @Description
 * @Author sq
 * @Date: 2020/8/9 19:14
 * @Version 1.0
 */
public interface CartService {
    /**
     * 根据用户查询购物车
     * @param memberId
     * @return
     */
    List<OmsCartItem> getCartsByUser(String memberId);

    /**
     * 根据用户id和skuId查询购物车商品
     * @param memberId
     * @param skuId
     * @return
     */
    OmsCartItem getCartByUserAndSkuId(String memberId,String skuId);

    /**
     * 更新购物车
     * @param cartItem
     */
    void update(OmsCartItem cartItem);

    /**
     * 添加购物车
     * @param omsCartItem
     */
    void add(OmsCartItem omsCartItem);

    /**
     * 同步缓存中
     * @param memberId
     */
    void flushCartCache(String memberId);

    /**
     * 获取购物车列表信息
     * @param userId
     * @return
     */
    List<OmsCartItem> cartList(String userId);

    /**
     * 修改商品状态
     *
     * @param omsCartItem
     */
    void checkCart(OmsCartItem omsCartItem);

    /**
     * 删除购物车商品
     * @param productSkuId
     * @param memberId
     */
    void delCart(String productSkuId,String memberId);
}
