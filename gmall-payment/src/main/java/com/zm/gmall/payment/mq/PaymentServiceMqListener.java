package com.zm.gmall.payment.mq;

import com.zm.gmall.bean.PaymentInfo;
import com.zm.gmall.service.PaymentService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Reference
    PaymentService paymentService;


    @JmsListener(destination = "PAYMENT-CHECK-QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentCheckResult(MapMessage mapMessage) throws JMSException {
        String to_trade_no = mapMessage.getString("to_trade_no");
        Integer count = Integer.parseInt(""+ mapMessage.getString("count"));

        //调用paymentService的支付宝接口检查交易状态
        System.out.println("进行延迟检查，调用支付检查的接口服务");
        Map<String,Object> requestMap = paymentService.checkAlipayPayment(to_trade_no);
        if(requestMap != null&&!requestMap.isEmpty()){
            String trade_status = (String) requestMap.get("trade_status");
            if(trade_status.equals("TRADE_SUCCESS")){
                //姐夫成功，更新 支付发送支付队列
                System.out.println("已经支付成功，调用支付服务，修改支付信息");
                String trade_no = (String) requestMap.get("trade_no");
                String out_trade_no = (String) requestMap.get("out_trade_no");
                String callbackContent = (String)requestMap.get("call_back_content");
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);//商品订单号
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(callbackContent);
                paymentInfo.setAlipayTradeNo(trade_no);//支付宝交易的订单号
                paymentService.updatePaymentInfo(paymentInfo);
                return;
            }
        }
        //调用延迟队列的次数
        if(count > 0){
            System.out.println("没用支付成功，检查剩余次数"+count+"急需发送");
            count--;
            paymentService.sendDelayPaymentResultCheckQueue(to_trade_no,count);
        }else {
            System.out.println("检查剩余次数用尽，结束检查");
        }
    }
}
