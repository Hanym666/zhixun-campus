package com.zhixun.item.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zhixun.security.jwt")
public record JwtProperties(String issuer, String secret) {
}
