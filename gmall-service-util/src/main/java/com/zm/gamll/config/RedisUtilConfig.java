package com.zm.gamll.config;



import com.zm.gamll.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisUtilConfig {
    //读取配置件中的redis的ip地址
    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:0}")
    private int port;
    @Value("${spring.redis.database:0}")
    private int database;
    //把reids对象交给spring管理
    @Bean
    public RedisUtil getRedisUtil(){
        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initPool(host,port,database);
        return redisUtil;
    }
}
