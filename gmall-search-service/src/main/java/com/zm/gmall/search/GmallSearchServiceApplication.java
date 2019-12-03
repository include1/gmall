package com.zm.gmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.zm.gmall"})
public class GmallSearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSearchServiceApplication.class, args);
    }

}
