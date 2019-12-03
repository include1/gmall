package com.zm.gmall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.zm.gmall")
public class GmallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSeckillApplication.class, args);
    }

}
