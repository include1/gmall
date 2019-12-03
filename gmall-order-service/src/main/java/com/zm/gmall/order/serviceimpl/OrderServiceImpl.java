package com.zm.gmall.order.serviceimpl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;

import com.alibaba.dubbo.config.annotation.Service;
import com.zm.gmall.bean.OmsOrder;
import com.zm.gmall.bean.OmsOrderItem;
import com.zm.gmall.bean.PmsSkuInfo;
import com.zm.gmall.order.dao.OrderItemMapper;
import com.zm.gmall.order.dao.OrderMapper;
import com.zm.gmall.order.util.ActiveMQUtil;
import com.zm.gmall.order.util.RedisUtil;
import com.zm.gmall.service.OrderService;
import com.zm.gmall.service.SkuService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Reference
    SkuService skuService;
    @Override
    public String checkTradeToken(String memberId, String tradeCode) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            //获取缓冲中tradeCode
            String key = "user:"+memberId+":tradeToken";
            String value = jedis.get(key);
            //jedis.del(value),此方法对于同步问题，没法解决,一定要使lua脚本（只要发现，就删除）
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(key), Collections.singletonList(tradeCode));
            if(eval != null&&eval != 0){
                return "success";
            }else{
                return "fail";
            }

        }finally {
            jedis.close();
        }
    }

    @Override
    public String getTradeToken(String memberId) {
        //获取Jedis
        Jedis jedis =null;
        String randomValue = null;
        try {
            jedis = redisUtil.getJedis();
            //设置key,和一个随机数
            randomValue = UUID.randomUUID().toString();
            String key = "user:"+memberId+":tradeToken";
            jedis.setex(key, 60 * 15, randomValue);
        }finally {
            jedis.close();
        }
        return randomValue;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {
        boolean b = false;
        PmsSkuInfo skuInfoById = skuService.getSkuInfoById(productSkuId);
        if(skuInfoById.getPrice().compareTo(productPrice) == 0){
            b = true;
        }
        return b;
    }

    @Override
    public void addOrderItem(OmsOrder omsOrder) {
        //添加order
        orderMapper.insertSelective(omsOrder);
        System.out.println(omsOrder.getId());
        //添加OrderItem
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(omsOrder.getId());
            orderItemMapper.insertSelective(omsOrderItem);
        }
    }

    @Override
    public OmsOrder getOrderByOrderSn(String tradeSn) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(tradeSn);
        OmsOrder omsOrder1 = orderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public OmsOrderItem getOrderItemByOrdrSn(String tradeSn) {
        OmsOrderItem omsOrderItem = new OmsOrderItem();
        omsOrderItem.setOrderSn(tradeSn);
        OmsOrderItem omsOrderItem1 = orderItemMapper.selectOne(omsOrderItem);
        return omsOrderItem1;
    }

    @Override
    public void updateOrderStatus(OmsOrder omsOrder) {
        omsOrder.setStatus(new BigDecimal("1"));
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());

        //使用中间件mq生产消息给库存服务，
        Connection connection = null;
        Session session = null;
        try {
            //创建消息中间件连接工厂
            ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
            connection = connectionFactory.createConnection();
            connection.start();
            //是否要开启事务，true，表示开启，
            session = connection.createSession(true,Session.SESSION_TRANSACTED);
            //创建一个队列,并设置标题名
            Queue queue = session.createQueue("ORDER-PAY-QUEUE");
            //设置一个消息的内容
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            //activeMQMapMessage.setString();
            //创建一个消息生产者
            MessageProducer messageProducer = session.createProducer(queue);
            orderMapper.updateByExampleSelective(omsOrder,example);
            //发送消息
            messageProducer.send(activeMQMapMessage);
            //提交事务，该消息才能放到队列中
            session.commit();
        }catch (Exception e){

            e.printStackTrace();
        }finally {
            //关闭资源
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
