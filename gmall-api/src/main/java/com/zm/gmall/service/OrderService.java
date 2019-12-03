package com.zm.gmall.service;

import com.zm.gmall.bean.OmsOrder;
import com.zm.gmall.bean.OmsOrderItem;

import java.math.BigDecimal;

public interface OrderService {
    String checkTradeToken(String memberId, String tradeCode);
    String getTradeToken(String memberId);
    boolean checkPrice(String productSkuId, BigDecimal productPrice);

    void addOrderItem(OmsOrder omsOrder);

    OmsOrder getOrderByOrderSn(String tradeSn);

    OmsOrderItem getOrderItemByOrdrSn(String tradeSn);

    void updateOrderStatus(OmsOrder omsOrder);
}
