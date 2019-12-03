package com.zm.gmall.redissonTest;

import com.alibaba.dubbo.common.utils.StringUtils;
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
        RLock lock = redissonClient.getLock("lock");//声明锁
        lock.lock();//开启锁
        try{
            String v = jedis.get("k");
            if(StringUtils.isBlank(v)){
                v = "1";
            }
            System.out.println("**** "+v);
            jedis.set("k",(Integer.parseInt(v)+1)+"");
            jedis.close();
        }finally {
            lock.unlock();//解锁
        }

        return "success";
    }
}
