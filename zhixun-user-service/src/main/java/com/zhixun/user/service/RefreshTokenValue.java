package com.zhixun.user.service;

public record RefreshTokenValue(String token, long expiresInSeconds) {
}
