package com.zm.gmall.seckill.controller;


import com.zm.gmall.seckill.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    RedisUtil redisUtil;
    //对应浏览器传来的请求
    @RequestMapping("kill")
    @ResponseBody
    public String kill(){
        //获取redisUtil客户端
        Jedis jedis = redisUtil.getJedis();
        //获取数量,监控商品的数量，保持数据一致性,必须在获取数量之前
        jedis.watch("106");
        Integer count = Integer.parseInt(jedis.get("106"));


        //判断数量
        if(count > 0){
            //开启事务
            Transaction multi = jedis.multi();
            //抢购商品
            multi.incrBy("106",-1);
            //提交事务
            List<Object> exec = multi.exec();
            if(exec != null&&exec.size()>0) {
                System.out.println("当前数量为" + count + "-某用户成功，已经抢购的数量为" + (1000 - count));
            }else{
                System.out.println("当前数量为"+count+"某用户抢购失败");
            }
        }
        //关闭jedis资源
        jedis.close();
        return "秒杀";
    }
}
