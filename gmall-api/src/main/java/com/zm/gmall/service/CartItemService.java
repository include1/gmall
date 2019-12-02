package com.zm.gmall.service;

import com.zm.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartItemService {
    OmsCartItem selectOmsCartItemByUser(String mumberId, String skuId);

    void addOmsCartItem(OmsCartItem omsCartItem);

    void modifyOmsCartItem(OmsCartItem omsCartItem);

    void flushCartCache(String numberId);

    List<OmsCartItem> getCartListById(String memberId);

    void checkCart(OmsCartItem omsCartItem);

    void delCartItem(String memberId);
}
