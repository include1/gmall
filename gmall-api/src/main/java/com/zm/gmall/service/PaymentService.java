package com.zm.gmall.service;

import com.zm.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    public void savePaymentInfo(PaymentInfo paymentInfo);
    public void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String tradeSn,int count);

    Map<String, Object> checkAlipayPayment(String to_trade_no);
}
