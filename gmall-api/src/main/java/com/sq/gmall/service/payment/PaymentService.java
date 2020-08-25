package com.sq.gmall.service.payment;

import com.sq.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * @Description
 * @Author sq
 * Created by sq on 2020/8/20 15:01
 */
public interface PaymentService {
    /**
     * 保存用户支付信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 更新支付信息
     * @param paymentInfo
     */
    void updatePayment(PaymentInfo paymentInfo);

    /**
     * 发送延迟消息查询支付宝的支付状态
     * @param orderId
     * @param count
     */
    void sendDelayPaymentResultCheckQueue(String orderId, int count);

    /**
     * 向支付宝发送支付查询
     * @param orderSn
     * @return
     */
    Map<String, Object> checkAlipayPayment(String orderSn);
}
