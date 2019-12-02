package com.zm.gmall.cart.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.zm.gmall.annotations.LoginRequired;
import com.zm.gmall.bean.OmsCartItem;
import com.zm.gmall.bean.PmsSkuInfo;
import com.zm.gmall.bean.PmsSkuSaleAttrValue;
import com.zm.gmall.cart.util.CookieUtil;
import com.zm.gmall.service.CartItemService;
import com.zm.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
@CrossOrigin
public class CartController {
    List<OmsCartItem> omsCartItemList = new ArrayList<>();
    @Reference
    SkuService skuService;
    @Reference
    CartItemService cartItemService;

    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String skuId,String isChecked,ModelMap modelMap,HttpServletRequest request,HttpServletResponse response){

        //获取用户信息
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //判断是否登录****
        if(!StringUtils.isBlank(memberId)){
            //已登录
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            omsCartItem.setProductSkuId(skuId);
            omsCartItem.setIsChecked(isChecked);
            cartItemService.checkCart(omsCartItem);
            //查询更新后的数据
           omsCartItemList = cartItemService.getCartListById(memberId);
        }else{
            //未登录,查询cookie中的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(!StringUtils.isBlank(cartListCookie)) {
               omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //更改单个是否选中状态
                for (OmsCartItem omsCartItem : omsCartItemList) {
                    if(skuId != null) {//某个商品的选择
                        if (omsCartItem.getProductSkuId().equals(skuId)) {
                            omsCartItem.setIsChecked(isChecked);
                        }
                    }else{//这是全选或全不选
                        omsCartItem.setIsChecked(isChecked);
                    }
                }
                CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(omsCartItemList),60*60*72,true);
            }
       }
        for (OmsCartItem omsCartItem : omsCartItemList) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        BigDecimal totalAccoument = totalAccoument(omsCartItemList);
        modelMap.put("totalAccoument",totalAccoument);
        //显示购物车的列表信息
        modelMap.put("cartList",omsCartItemList);
        return "cartListInner";
    }

    private BigDecimal totalAccoument(List<OmsCartItem> omsCartItems) {
        //初始化数据
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if(omsCartItem.getIsChecked().equals("1")){
                bigDecimal = bigDecimal.add(totalPrice);
            }
        }
        return bigDecimal;
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        //获取用户信息
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //判断用户是否登录
        if(StringUtils.isBlank(memberId)){
            //未登录，查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(!StringUtils.isBlank(cartListCookie)) {
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }else{
            //登录，查询缓冲、数据库
            omsCartItemList = cartItemService.getCartListById(memberId);
        }
        //计算价格
        if(omsCartItemList != null){
            for (OmsCartItem omsCartItem : omsCartItemList) {
                omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            }
            //算出总价格
            BigDecimal totalAccoument = totalAccoument(omsCartItemList);
            modelMap.put("totalAccoument",totalAccoument);

        }
        modelMap.put("cartList",omsCartItemList);
        return "cartList";
    }

    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId,Integer quantity, HttpServletRequest request, HttpServletResponse response,HttpSession session)//数量：quantity
    {
        //获取通过skuId获取商品的详细信息
        PmsSkuInfo skuInfoById = skuService.getSkuInfoById(skuId);
        //把商品放入购物车中
        OmsCartItem omsCartItem = new OmsCartItem();
        //初始化内容
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfoById.getSkuSaleAttrValueList();
        for(int i = 0; i < skuSaleAttrValueList.size(); i++) {
            if (i == 0) {
                omsCartItem.setSp1(skuSaleAttrValueList.get(0).getSaleAttrValueName());
            }else if(i == 1) {
                omsCartItem.setSp2(skuSaleAttrValueList.get(1).getSaleAttrValueName());
            }else if(i == 2){
                omsCartItem.setSp3(skuSaleAttrValueList.get(2).getSaleAttrValueName());
            }
        }
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(new BigDecimal("1"));
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfoById.getPrice());
        omsCartItem.setProductBrand("");
        omsCartItem.setProductId(skuInfoById.getSpuId());
        omsCartItem.setProductName(skuInfoById.getSkuName());
        omsCartItem.setProductCategoryId(skuInfoById.getCatalog3Id());
        omsCartItem.setProductPic(skuInfoById.getSkuDefaultImg());
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        omsCartItem.setProductSn("1111111111111");
        omsCartItem.setIsChecked("1");
        //获取用户信息
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //未登录时，把商品信息写到浏览器中cookie中
        if(StringUtils.isBlank(memberId)){
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);//isDecoder：表示是否使用utf-8
            if(cartListCookie == null){
                //购物车为空
                omsCartItemList.add(omsCartItem);
            }else{
                //购物车不为空
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断是否有重复数据
                boolean b = isRepeatValue(omsCartItemList,omsCartItem);
                if(b){
                   //有 ,数量相加
                    for (OmsCartItem cartItem : omsCartItemList) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                               cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }
                }else{
                    //无
                    omsCartItemList.add(omsCartItem);
                }
            }
            //将数据放入cookie，更新cookie中的数据
            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItemList),60*60*72,true);
        }else {//登录时，调用service层的方法把商品信息写入的数据库和缓冲中
             //应该吧缓冲中的数据更新的数据库中
            //查询数据库信息，判断是否重复
            OmsCartItem omsCartItemFromDB = cartItemService.selectOmsCartItemByUser(memberId,skuId);
            if(omsCartItemFromDB == null){
                //没有重复数据或无数据
                // 添加相应的id和numberID
                omsCartItem.setMemberId(memberId);
                cartItemService.addOmsCartItem(omsCartItem);
            }else{
                //有重复数据
                //更新数据
                omsCartItemFromDB.setQuantity(omsCartItemFromDB.getQuantity().add(omsCartItem.getQuantity()));
                cartItemService.modifyOmsCartItem(omsCartItemFromDB);
            }
            //更新redis缓存
            cartItemService.flushCartCache(memberId);
        }
        session.setAttribute("skuInfo",skuInfoById);
        //实现利用cookie的缓冲数据放到购物车中
        return "redirect:/success.html";
    }
    //判读cookie中的已经有了这个数据
    private boolean isRepeatValue(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = false;
        for (OmsCartItem cartItem : omsCartItems) {
            if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
               b = true;
            }
        }
        return b;
    }
}
