package com.zm.gmall.payment.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.zm.gmall.bean.PaymentInfo;
import com.zm.gmall.payment.dao.PaymentMapper;
import com.zm.gmall.payment.util.ActiveMQUtil;
import com.zm.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTempQueue;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;


@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);

    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {

        //进行幂等性检查
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoResult = paymentInfoParam = paymentMapper.selectOne(paymentInfoParam);
        //判断是否已经支付成功
        if(!StringUtils.isBlank(paymentInfoResult.getPaymentStatus())&&paymentInfoResult.getPaymentStatus().equals("已支付")){
            return;
        }else{
            Example example = new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());

            //使用消息中间件，在更新支付信息的同时，把消息发送出去。使多个服务并发执行
            //创建连接对象

            Connection connection = null;
            Session session = null;

            try {
                ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
                connection = connectionFactory.createConnection();
                connection.start();
                //是否要开启事务，true，表示开启，
                session = connection.createSession(true,Session.SESSION_TRANSACTED);

                Queue queue = session.createQueue("PAYMENT-SUCCESS-QUEUE");
                MessageProducer messageProducer = session.createProducer(queue);
                //hash结构
                MapMessage activeMQMapMessage = new ActiveMQMapMessage();
                activeMQMapMessage.setString("to_trade_no",paymentInfo.getOrderSn());
                messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
                //发送消息给订单服务，同步服务，使用mq进行管理消息队列
                //更新支付信息同时，发送消息给订单服务
                paymentMapper.updateByExampleSelective(paymentInfo,example);
                messageProducer.send(activeMQMapMessage);
                //提交事务
                session.commit();
            }catch (Exception e){
                try {
                    //事务回滚
                    session.rollback();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }finally {
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String tradeSn,int count) {
        Connection connection = null;
        Session session = null;
        try{
            //生成一个延迟队列的消息，该队列会定时的向支付宝发送消息，判断用户的交易状态，防止由于某种原因支付宝没有返回成功支付的消息
            //创建连接
            connection = activeMQUtil.getConnectionFactory().createConnection();
            //创建一个会话,是否开启事务
            session = connection.createSession(true,Session.SESSION_TRANSACTED);
            //创建一个延迟队列
            Queue queue = session.createQueue("PAYMENT-CHECK-QUEUE");
            //创建一个生产者
            MessageProducer producer = session.createProducer(queue);
            //创建一个消息
            MapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setStringProperty("to_trade_no",tradeSn);
            activeMQMapMessage.setString("count",String.valueOf(count));
            //设置消息的执行时间
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);
            //发送
            producer.send(activeMQMapMessage);
            //提交事务，才能发送到队列中
            session.commit();
        }catch (Exception e){
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }finally {
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkAlipayPayment(String to_trade_no) {
        Map<String,Object> resultMap = new HashMap<>();
        //传递参数给支付宝的接口
        AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();
        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",to_trade_no);
        alipayTradeQueryRequest.setBizContent(JSON.toJSONString(requestMap));

        //接受支付宝的响应参数
        AlipayTradeQueryResponse response = null;
        try{
            response = alipayClient.execute(alipayTradeQueryRequest);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("交易已创建，支付成功");
            resultMap.put("to_trade_no",response.getOutTradeNo());
            resultMap.put("trade_no",response.getTradeNo());
            resultMap.put("trade_status",response.getTradeStatus());
            requestMap.put("call_back_content",response.getMsg());
        }else {
            System.out.println("交易未创建，支付失败");
        }
        return requestMap;
    }
}
