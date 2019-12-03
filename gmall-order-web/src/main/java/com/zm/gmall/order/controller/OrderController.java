package com.zm.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zm.gmall.annotations.LoginRequired;
import com.zm.gmall.bean.*;
import com.zm.gmall.service.CartItemService;
import com.zm.gmall.service.OrderService;
import com.zm.gmall.service.UmsMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    UmsMemberService umsMemberService;
    @Reference
    CartItemService cartItemService;
    @Reference
    OrderService orderService;

    @RequestMapping("submitOrder")
    @LoginRequired
    public ModelAndView submitOrder(String recieveAddressId, String tradeCode, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response){
        //获得用户的相关信息
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //获取该用户购物车中所用的商品信息
        List<OmsCartItem> cartListById = cartItemService.getCartListById(memberId);
        //验证tradeCode是否合法，防止同步请求，对数据库造成多次写入
        String success = orderService.checkTradeToken(memberId,tradeCode);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        OmsOrder omsOrder = new OmsOrder();
        //用户属性
        UmsMember umsMemberById = umsMemberService.getUmsMemberById(memberId);
        omsOrder.setModifyTime(new Date());
        omsOrder.setMemberId(memberId);
        omsOrder.setMemberUsername(umsMemberById.getUsername());
        //订单属性
        omsOrder.setAutoConfirmDay(new BigDecimal("7"));
        omsOrder.setDiscountAmount(null);
        omsOrder.setCommentTime(new Date());
        omsOrder.setCreateTime(new Date());
        String tradeSn = "gmall";
        tradeSn = tradeSn + System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMDDhhmmss");
        tradeSn = tradeSn + simpleDateFormat.format(new Date());
        omsOrder.setOrderSn(tradeSn);
        omsOrder.setNote("快点发货");
        BigDecimal totalAccoument = totalAccoument(cartListById);
        omsOrder.setPayAmount(totalAccoument);
        omsOrder.setOrderType(new BigDecimal("1"));

        //查询的收货地址
        UmsMemberReceiveAddress receiveAddress = umsMemberService.getReceiveAddrById(recieveAddressId);
        omsOrder.setReceiverCity(receiveAddress.getCity());
        omsOrder.setReceiverDetailAddress(receiveAddress.getDetailAddress());
        omsOrder.setReceiverName(receiveAddress.getName());
        omsOrder.setReceiverPhone(receiveAddress.getPhoneNumber());
        omsOrder.setReceiverPostCode(receiveAddress.getPostCode());
        omsOrder.setReceiverProvince(receiveAddress.getProvince());
        omsOrder.setReceiverRegion(receiveAddress.getRegion());
        //当前时间+1
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        Date time = calendar.getTime();
        omsOrder.setReceiveTime(time);
        //订单对象的属性注入
        if("success".equals(success)){
            //查询购物车选中的商品信息
            for (OmsCartItem omsCartItem : cartListById) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    //校验价格,如果价格不一样，返回错误页面
                    boolean b = orderService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if(b == false){
                        ModelAndView modelAndView = new ModelAndView("tradeFail");
                        return modelAndView;
                    }
                    //校验库存，通过库存系统
                    //--------------------------------------------------等待开发
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setOrderSn(tradeSn);//订单号
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setStock(new BigDecimal("1"));
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(null);
                    omsOrderItem.setSp1(omsCartItem.getSp1());
                    omsOrderItem.setSp2(omsCartItem.getSp2());
                    omsOrderItem.setSp3(omsCartItem.getSp3());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);
            //订单商品信息和订单信息写入到数据库
            orderService.addOrderItem(omsOrder);
            //删除购物车中已选中的商品
            cartItemService.delCartItem(memberId);
            ModelAndView modelAndView = new ModelAndView("redirect:http://localhost:8088/index");
            modelAndView.addObject("totalAccoument",totalAccoument);
            modelAndView.addObject("tradeSn",tradeSn);
            return modelAndView;
        }else{
            ModelAndView modelAndView = new ModelAndView("tradeFail");
            return modelAndView;
        }
    }
    @RequestMapping("toTrade")
    @LoginRequired
    public String toTrade(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response){
        //获得用户的相关信息
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");

        //获取用户的收货地址
        List<UmsMemberReceiveAddress> receiveAddrs= umsMemberService.getReceiveAddrByMemberId(memberId);

        for (UmsMemberReceiveAddress receiveAddr : receiveAddrs) {
            String address = receiveAddr.getProvince()+"\t"+receiveAddr.getCity()+"\t"+receiveAddr.getRegion()+"\t"+receiveAddr.getDetailAddress()+"\t"+receiveAddr.getPhoneNumber();
            receiveAddr.setAbsoluteAddress(address);
        }
        //把购物车选中选中的商品放到订单中
        List<OmsCartItem> omsCartItems = cartItemService.getCartListById(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            if(omsCartItem.getIsChecked().equals("1")){
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductId(omsCartItem.getProductId());
                omsOrderItem.setSp1(omsCartItem.getSp1());
                omsOrderItem.setSp2(omsCartItem.getSp2());
                omsOrderItem.setSp3(omsCartItem.getSp3());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItem.setProductPrice(omsCartItem.getPrice());
                omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                omsOrderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                omsOrderItem.setStock(new BigDecimal("10"));
                omsOrderItems.add(omsOrderItem);
            }

        }
        modelMap.put("nickName",nickname);
        modelMap.put("userAddressList",receiveAddrs);
        modelMap.put("omsOrderItems",omsOrderItems);
        modelMap.put("totalAmount",totalAccoument(omsCartItems));
        //生成一个tradeToken，给交易页面
        String tradeCode = orderService.getTradeToken(memberId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }
    private BigDecimal totalAccoument(List<OmsCartItem> omsCartItems) {
        //初始化数据
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if(omsCartItem.getIsChecked().equals("1")){
                bigDecimal = bigDecimal.add(totalPrice);
            }
        }
        return bigDecimal;
    }
}
