package com.zhixun.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zhixun")
public class ZhixunGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhixunGatewayApplication.class, args);
    }
}
