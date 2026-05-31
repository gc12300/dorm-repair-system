package com.dormrepair;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dormrepair.mapper")
public class DormRepairApplication {
    public static void main(String[] args) {
        SpringApplication.run(DormRepairApplication.class, args);
    }
}
