package com.zm.gmall.gamllredissiontest.redissonTest;


import com.zm.gmall.util.RedisUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
public class RedissonController {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RedisUtil redisUtil;

    @RequestMapping("redission")
    @ResponseBody
    public String redission(){
        Jedis jedis = redisUtil.getJedis();
        RLock lock = redissonClient.getLock("lock");
        return null;
    }
}
