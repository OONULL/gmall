package com.sq.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.sq.gmall.bean.PaymentInfo;
import com.sq.gmall.mq.ActiveMQUtil;
import com.sq.gmall.payment.mapper.PaymentMapper;
import com.sq.gmall.service.payment.PaymentService;
import com.sq.gmall.util.MQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * @title: PaymentServiceImpl
 * @Description
 * @Author sq
 * @Date: 2020/8/20 15:07
 * @Version 1.0
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private ActiveMQUtil activeMQUtil;
    @Autowired
    private AlipayClient alipayClient;
    private PaymentInfo paymentInfoParam;

    /**
     * 保存用户支付信息
     * @param paymentInfo
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    /**
     * 更新支付信息
     * @param paymentInfo
     */
    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
       //幂等性检查(是否已经执行过更新操作)
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoResult = paymentMapper.selectOne(paymentInfoParam);
        if(StringUtils.isBlank(paymentInfoResult.getPaymentStatus())||!"已支付".equals(paymentInfoResult.getPaymentStatus())){
            Session session = null;
            try {
                Example example = new Example(PaymentInfo.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("orderSn", paymentInfo.getOrderSn());
                paymentMapper.updateByExampleSelective(paymentInfo,example);
                //支付成功调用mq发送支付成功消息
                ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
                activeMQMapMessage.setString("orderSn",paymentInfo.getOrderSn());
                session = MQUtil.sendQueue(activeMQUtil,"PAYHMENT_SUCCESS_QUEUE",activeMQMapMessage);
                session.commit();
            } catch (Exception e) {
                if(session!=null){
                    try {
                        session.rollback();
                    } catch (JMSException jmsException) {
                        jmsException.printStackTrace();
                    }
                }
                e.printStackTrace();
             }
        }
    }

    /**
     * 发送延迟消息查询支付宝的支付状态
     * @param orderId
     * @param count
     */
    @Override
    public void sendDelayPaymentResultCheckQueue(String orderId, int count) {
        Session session = null;
        try {
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderSn",orderId);
            activeMQMapMessage.setString("count", String.valueOf(count));
            //设置延迟队列(参数1:延迟设置常量  参数2:延迟时间(毫秒))
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*60);
            session = MQUtil.sendQueue(activeMQUtil,"PAYMENT_CHECK_QUEUE",activeMQMapMessage);
            session.commit();
        } catch (JMSException e) {
            if(session!=null){
                try {
                    session.rollback();
                } catch (JMSException jmsException) {
                    jmsException.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    /**
     * 向支付宝发送支付查询
     * @param orderSn
     * @return
     */
    @Override
    public Map<String, Object> checkAlipayPayment(String orderSn) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",orderSn);
        String param = JSON.toJSONString(map);
        request.setBizContent(param);
        AlipayTradeQueryResponse response = null;

        Map<String,Object> resultMap = new HashMap<>();
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //调用成功
        if(response.isSuccess()){
            resultMap.put("out_trade_no",response.getOutTradeNo());
            resultMap.put("trade_no",response.getTradeNo());
            resultMap.put("trade_status",response.getTradeStatus());
            resultMap.put("callBackContent",response.getMsg());
        }
        return resultMap;
    }
}
