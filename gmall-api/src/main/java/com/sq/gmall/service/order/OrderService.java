package com.sq.gmall.service.order;

import com.sq.gmall.bean.OmsOrder;

/**
 * @Description 订单结算
 * @Author sq
 * Created by sq on 2020/8/17 15:16
 */
public interface OrderService {
    /**
     * 生成交易码
     * @param memberId
     * @return
     */
    String generateTradeCode(String memberId);

    /**
     * 校验交易码
     * @param memberId
     * @return
     */
    String checkTradeCode(String memberId,String tradeCode);

    /**
     * 保存订单并删除购物车商品
     * @param omsOrder
     * @return
     */
    String saveOrder(OmsOrder omsOrder);

    /**
     * 根据用户id获取用户订单
     * @param memberId
     * @return
     */
    OmsOrder findByMemberId(String orderId,String memberId);

    /**
     * 根据外部订单号获取订单
     * @param orderId
     * @return
     */
    OmsOrder findByOrderSn(String orderId);

    /**
     * 更新订单状态
     * @param omsOrder
     */
    void updateOrder(OmsOrder omsOrder);
}
