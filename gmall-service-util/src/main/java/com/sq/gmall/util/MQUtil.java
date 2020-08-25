package com.sq.gmall.util;

import com.sq.gmall.mq.ActiveMQUtil;

import javax.jms.*;

/**
 * @title: MQUtil
 * @Description
 * @Author sq
 * @Date: 2020/8/21 17:30
 * @Version 1.0
 */
public class MQUtil {

    /**
     * 创建并发送支付成功消息
     * @param activeMQUtil
     * @param Message
     * @return
     */
    public static Session sendQueue(ActiveMQUtil activeMQUtil,String QueueName,Message Message){
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            //开启可回滚消息队列
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            //创建消息队列
            assert session != null;
            Queue payhmentSuccessQueue = session.createQueue(QueueName);
            MessageProducer producer = session.createProducer(payhmentSuccessQueue);

            producer.send(Message);
        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            if(connection!=null){
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
        return session;
    }
}
