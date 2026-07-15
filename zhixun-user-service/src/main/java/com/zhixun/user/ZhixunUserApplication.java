package com.zhixun.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zhixun.user.mapper")
@SpringBootApplication(scanBasePackages = "com.zhixun")
public class ZhixunUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhixunUserApplication.class, args);
    }
}
