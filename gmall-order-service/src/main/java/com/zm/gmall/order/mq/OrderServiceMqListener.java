package com.zm.gmall.order.mq;


import com.zm.gmall.bean.OmsOrder;
import com.zm.gmall.order.serviceimpl.OrderServiceImpl;
import com.zm.gmall.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
//@Component：把普通的pojo的实例对象放到spring中
public class OrderServiceMqListener {
    //private OrderService orderService = new OrderServiceImpl();

    @JmsListener(destination = "PAYMENT-SUCCESS-QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        String to_trade_no = mapMessage.getString("to_trade_no");
        System.out.println(to_trade_no);
        //更新订单信息
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(to_trade_no);
        //orderService.updateOrderStatus(omsOrder);
        System.out.println("success");
    }

}
