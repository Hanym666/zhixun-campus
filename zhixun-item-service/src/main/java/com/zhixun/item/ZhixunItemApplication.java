package com.zhixun.item;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zhixun.item.mapper")
@SpringBootApplication(scanBasePackages = "com.zhixun")
public class ZhixunItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhixunItemApplication.class, args);
    }
}
