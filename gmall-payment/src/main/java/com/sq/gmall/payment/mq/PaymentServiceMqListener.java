package com.sq.gmall.payment.mq;

import com.sq.gmall.bean.PaymentInfo;
import com.sq.gmall.service.payment.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

/**
 * @title: PaymentServiceMqListener
 * @Description
 * @Author sq
 * @Date: 2020/8/22 8:09
 * @Version 1.0
 */
@Component
public class PaymentServiceMqListener {
    @Autowired
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentCheckResult(MapMessage mapMessage) throws JMSException {
        String orderSn = mapMessage.getString("orderSn");
        int count = 0;
        if (StringUtils.isNotBlank(mapMessage.getString("count"))) {
            count = Integer.parseInt(mapMessage.getString("count"));
        }
        //调用paymentService的支付宝检查接口
        Map<String, Object> resultMap = paymentService.checkAlipayPayment(orderSn);
        //判断是否获取到支付宝交易信息
        if (!resultMap.isEmpty()) {
            String tradeStatus = (String) resultMap.get("trade_status");
            //根据支付状态结果,判断是否进行下次延迟任务还是支付成功更新数据和后续任务
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn((String) resultMap.get("out_trade_no"));
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no"));//支付宝交易凭证
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent((String) resultMap.get("callBackContent"));//回调请求字符串
                paymentService.updatePayment(paymentInfo);
                return;
            }
        }
        if (count > 0) {
            //继续发送延迟检查任务,计算延迟时间
            count--;
            paymentService.sendDelayPaymentResultCheckQueue(orderSn, count);
        } else {
            //超过次数,
        }
    }
}
