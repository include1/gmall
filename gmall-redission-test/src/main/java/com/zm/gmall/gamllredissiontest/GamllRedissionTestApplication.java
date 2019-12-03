package com.zm.gmall.gamllredissiontest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.zm.gmall"})
public class GamllRedissionTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamllRedissionTestApplication.class, args);
    }

}
