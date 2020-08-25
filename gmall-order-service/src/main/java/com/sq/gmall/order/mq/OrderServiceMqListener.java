package com.sq.gmall.order.mq;

import com.sq.gmall.bean.OmsOrder;
import com.sq.gmall.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @title: OrderServiceMqListener
 * @Description 监听消息队列
 * @Author sq
 * @Date: 2020/8/21 17:44
 * @Version 1.0
 */
@Component
public class OrderServiceMqListener {

    @Autowired
    private OrderService orderService;
    /**
     * 监听消息队列PAYHMENT_SUCCESS_QUEUE
     */
    @JmsListener(destination = "PAYHMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")//消息监听器连接工厂
    public void updateOrderProcessStatus(MapMessage mapMessage){
        try {
            //获取消息队列中的外部订单号
            String orderSn = mapMessage.getString("orderSn");
            //更新订单状态业务
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderSn(orderSn);
            orderService.updateOrder(omsOrder);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
