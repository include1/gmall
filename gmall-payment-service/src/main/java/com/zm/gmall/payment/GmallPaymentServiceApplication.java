package com.zm.gmall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.zm.gmall")
public class GmallPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPaymentServiceApplication.class, args);
    }

}
