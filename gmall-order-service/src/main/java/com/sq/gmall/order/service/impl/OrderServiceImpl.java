package com.sq.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.sq.gmall.bean.OmsOrder;
import com.sq.gmall.bean.OmsOrderItem;
import com.sq.gmall.mq.ActiveMQUtil;
import com.sq.gmall.order.mapper.OmsOrderItemMapper;
import com.sq.gmall.order.mapper.OmsOrderMapper;
import com.sq.gmall.service.cart.CartService;
import com.sq.gmall.service.order.OrderService;
import com.sq.gmall.util.MQUtil;
import com.sq.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @title: OrderServiceImpl
 * @Description
 * @Author sq
 * @Date: 2020/8/17 15:16
 * @Version 1.0
 */
@Service(interfaceClass=OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OmsOrderMapper omsOrderMapper;
    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;
    @Reference
    private CartService cartService;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    /**
     * 生成交易码
     * @param memberId
     * @return
     */
    @Override
    public String generateTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeCode = null;
        try {
            tradeCode = UUID.randomUUID().toString();
            jedis = redisUtil.getJedis();
            jedis.setex("user:"+memberId+":tradeCode",60*15,tradeCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }

        return tradeCode;
    }
    /**
     * 校验交易码
     * @param memberId
     * @return
     */
    @Override
    public String checkTradeCode(String memberId,String tradeCode) {
        Jedis jedis = null;
        String tradeKey = "user:"+memberId+":tradeCode";
        try {
            jedis = redisUtil.getJedis();
            //使用lua脚本防止高并发同时进入代码
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),
                    Collections.singletonList(tradeCode));
            if(eval!=null && eval!=0){
               //校验成功
                return "success";
            }else {
                return "fail";
            }
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }

    }

    /**
     * 保存订单并删除购物车商品
     * @param omsOrder
     * @return
     */
    @Override
    @Transactional
    public String saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(omsOrder.getId());
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车商品
            cartService.delCart(omsOrderItem.getProductSkuId(),omsOrder.getMemberId());
        }
        return omsOrder.getId();
    }

    /**
     * 根据用户id获取用户订单
     * @param memberId
     * @return
     */
    @Override
    public OmsOrder findByMemberId(String memberId,String orderId) {
        OmsOrder omsOrder = omsOrderMapper.selectByPrimaryKey(orderId);
        if(StringUtils.isNotBlank(memberId)&&memberId.equals(omsOrder.getMemberId())){
            return omsOrder;
        }
        return null;
    }

    /**
     * 根据外部订单号获取订单
     * @param orderId
     * @return
     */
    @Override
    public OmsOrder findByOrderSn(String orderId) {
        Example example = new Example(OmsOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderSn",orderId);
        return omsOrderMapper.selectOneByExample(example);
    }

    /**
     * 更新订单状态并发送消息到消息队列
     * @param omsOrder
     */
    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        OmsOrder omsOrder1 = new OmsOrder();
        omsOrder1.setStatus("1");
        Session session =null;
        try {
            omsOrderMapper.updateByExampleSelective(omsOrder1,example);

            //发送订单已支付队列,提供给库存服务接收
            OmsOrder order = new OmsOrder();
            order.setOrderSn(omsOrder.getOrderSn());
            OmsOrder selectOne = omsOrderMapper.selectOne(order);
            Example example1 = new Example(OmsOrderItem.class);
            example1.createCriteria().andEqualTo("orderId",order.getId());
            List<OmsOrderItem> omsOrderItemList = omsOrderItemMapper.selectByExample(example1);
            order.setOmsOrderItems(omsOrderItemList);
            TextMessage mapMessage = new ActiveMQTextMessage();
            mapMessage.setText(JSON.toJSONString(selectOne));
            session = MQUtil.sendQueue(activeMQUtil,"ORDER_PAY_QUEUE",mapMessage);
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
