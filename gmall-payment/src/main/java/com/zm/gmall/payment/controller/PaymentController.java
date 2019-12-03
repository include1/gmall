package com.zm.gmall.payment.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zm.gmall.annotations.LoginRequired;
import com.zm.gmall.bean.OmsOrder;
import com.zm.gmall.bean.OmsOrderItem;
import com.zm.gmall.bean.PaymentInfo;

import com.zm.gmall.payment.conf.AlipayConfig;
import com.zm.gmall.service.OrderService;
import com.zm.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {


    @Reference
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;


    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = false)
    public String callback(HttpServletRequest request,ModelMap modelMap){
        //延迟队列检查支付成功后，返回一个支付成功的的一些信息
        String trade_no = request.getParameter("trade_no");//商品订单号
        String sign = request.getParameter("sign");//签名
        String out_trade_no = request.getParameter("out_trade_no");//支付宝交易的订单号
        String parameter = request.getParameter("total_amount");//支付的总价格
        String callbackContent = request.getQueryString();//url：request请求后面携带的参数
        //这个过程是通过远程接口进行验证，
        //暂时是模拟
        if(!StringUtils.isBlank(sign)){
            //更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);//商品订单号
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(callbackContent);
            paymentInfo.setAlipayTradeNo(trade_no);//支付宝交易的订单号
            paymentService.updatePaymentInfo(paymentInfo);
        }
        //此时订单并没有完成须要调用-》订单服务-》库存服务-》物流服务
        //使用消息中间件来解决服务并发问题
        return "success";
    }
    @RequestMapping("mx/submit")
    @LoginRequired
    @ResponseBody
    public String mx(String totalAccoument, HttpServletRequest request ,String tradeSn, ModelMap modelMap){
        String  memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        return null;
    }
    @RequestMapping("alipay/submit")
    @LoginRequired
    @ResponseBody
    public String alipay(String totalAccoument, HttpServletRequest request ,String tradeSn, ModelMap modelMap){
//        String  memberId = (String) request.getAttribute("memberId");
//        String nickname = (String) request.getAttribute("nickname");
        String form="";
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //回调地址
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",tradeSn);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount","0.01");
        map.put("subject","测试支付接口名");
        String param = JSON.toJSONString(map);
        alipayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //获取订单中的数据
        OmsOrder omsOrder = orderService.getOrderByOrderSn(tradeSn);
        //查询订单商品中的数据
        OmsOrderItem omsOrderItem = orderService.getOrderItemByOrdrSn("gmall1574520717439201911327105157");
        //保存支付订单信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
       // paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(tradeSn);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setTotalAmount(totalAccoument);
        paymentInfo.setSubject(omsOrderItem.getProductName());
        //保存
        paymentService.savePaymentInfo(paymentInfo);
        //发送一个延迟队列
        paymentService.sendDelayPaymentResultCheckQueue(tradeSn,5);
        return form;
    }
    @RequestMapping("index")
    @LoginRequired
    public String index(String totalAccoument, HttpServletRequest request ,String tradeSn, ModelMap modelMap){
        String  memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        modelMap.put("nickName",nickname);
        modelMap.put("totalAmount",totalAccoument);
        modelMap.put("orderId",tradeSn);

        return "index";
    }
}
