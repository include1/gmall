package com.zm.gmall.cart.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.zm.gmall.bean.OmsCartItem;
import com.zm.gmall.cart.dao.CartItemMapper;

import com.zm.gmall.cart.util.RedisUtil;
import com.zm.gmall.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

import static tk.mybatis.mapper.entity.Example.*;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    CartItemMapper cartItemMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem selectOmsCartItemByUser(String mumberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(mumberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = cartItemMapper.selectOne(omsCartItem);
        return  omsCartItem1;
    }

    @Override
    public void addOmsCartItem(OmsCartItem omsCartItem) {
        if(omsCartItem != null) {
            cartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void modifyOmsCartItem(OmsCartItem omsCartItem) {
        if (omsCartItem != null){
            Example example = new Example(OmsCartItem.class);
            example.createCriteria().andEqualTo("id", omsCartItem.getId());
            cartItemMapper.updateByExampleSelective(omsCartItem, example);
        }
    }

    public List<OmsCartItem> getOmsCartItemByMemberId(String memberId){
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItemList = cartItemMapper.select(omsCartItem);
        return  omsCartItemList;
    }
    @Override
    public void flushCartCache(String memberId) {
        //查询出表中的数据
        List<OmsCartItem> omsCartItemList = getOmsCartItemByMemberId(memberId);
        Jedis jedis = null;
        try {
            //redisUtil建立连接对象
            jedis = redisUtil.getJedis();
            //使用hash表进行key-value进行存储：通过Mape<key，Map<key,value>>,可以快速查询，和修改某一个购物车的属性
            Map<String, String> hashMap = new HashMap<>();
            for (OmsCartItem cartItem : omsCartItemList) {
                hashMap.put(cartItem.getProductSkuId(),JSON.toJSONString(cartItem));
            }
            jedis.del("user:" + memberId + ":cart");
            jedis.hmset("user:" + memberId + ":cart", hashMap);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }

    @Override
    public List<OmsCartItem> getCartListById(String memberId) {
        //通过用户id,查询购物车的商品，先从缓冲(redis)中获取数据
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        //获取redis的连接对象
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String key = "user:" + memberId + ":cart";
            List<String> hvals = jedis.hvals(key);
            //判读是否查询的数据
            if (hvals != null) {
                //查询到
                for (String str : hvals) {
                    OmsCartItem omsCartItem = JSON.parseObject(str, OmsCartItem.class);
                    omsCartItemList.add(omsCartItem);
                }
            } else {
                //未查询到，从DB中查询数据
                //开启redis分布式锁
                //生成一个随机数,解决缓冲击穿的问题（突然有大量请求访问同一个key,此时这个key失效，导致数据库访问压力过大出现宕机情况）
                String token = UUID.randomUUID().toString();
                String keyLock = "user:"+memberId+":lock";
                String setLock = jedis.set(keyLock, token, "nx", "px", 1000);
                //判断是否获取分布式锁
                if(!StringUtils.isBlank(setLock)&&"OK".equals(setLock)){
                    //获得锁
                    omsCartItemList = getOmsCartItemByMemberId(memberId);
                    //释放锁资源
                    String tokenValue = jedis.get(keyLock);
                    if(!StringUtils.isBlank(tokenValue)&&token.equals(tokenValue)){
                        jedis.del(keyLock);
                    }
                }else{
                    //未获得锁
                    Thread.sleep(1000);
                    return getCartListById(memberId);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            //可以打印日志
            return null;
        }finally {
            //关闭资源
            jedis.close();
        }
        return omsCartItemList;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        //更新数据
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        cartItemMapper.updateByExampleSelective(omsCartItem,example);
        //更新缓冲数据
        flushCartCache(omsCartItem.getMemberId());
    }

    @Override
    public void delCartItem(String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            Map<String, String> hashMap = new HashMap<>();
            List<OmsCartItem> cartList = getCartListById(memberId);
            for (OmsCartItem omsCartItem : cartList) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    //删除数据库中的数据
                    cartItemMapper.delete(omsCartItem);

                }else{
                    hashMap.put(omsCartItem.getProductSkuId(),JSON.toJSONString(omsCartItem));
                }
            }
            //删除缓冲中的数据
            jedis.del("user:" + memberId + ":cart");
            jedis.hmset("user:" + memberId + ":cart", hashMap);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }
}
