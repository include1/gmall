package com.zm.gmall.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zm.gmall.bean.OmsOrder;
import com.zm.gmall.order.util.RedisUtil;
import com.zm.gmall.service.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import tk.mybatis.spring.annotation.MapperScan;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan(basePackages = "com.zm.gmall")
public class GmallOrderServiceApplicationTests {

    @Autowired
    RedisUtil redisUtil;
    @Reference
    OrderService orderService;
    @Test
    public void contextLoads() {
        OmsOrder orderByOrderSn = orderService.getOrderByOrderSn("201809150101000001");
        System.out.println(orderByOrderSn);
    }

}
