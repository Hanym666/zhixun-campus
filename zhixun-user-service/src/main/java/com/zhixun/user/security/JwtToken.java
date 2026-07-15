package com.zhixun.user.security;

import java.time.Instant;

public record JwtToken(String value, Instant expiresAt) {
}
