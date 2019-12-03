package com.zm.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestMqTopic {
    public static void main(String[] args) {
        //与mq建立连接
        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = null;
        try {
            connection = connect.createConnection();

            connection.start();
            //创建一次回话开启事务
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列名，消息队列只能执行一次
           // Queue queue = session.createQueue("drink");
            //创建topic
            Topic topic = session.createTopic("speaking");
            //创建生产者
            MessageProducer producer = session.createProducer(topic);
            //创建消息对象，编写消息内容
            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("为中华民族伟大复兴而奋斗");
            //设置消息传输的模式
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            //把消息内容放入生产者中
            producer.send(textMessage);
            //提交事务
            session.commit();
            //关闭事务
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
