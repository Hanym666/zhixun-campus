package com.zhixun.user.service;

import com.zhixun.common.exception.BusinessException;
import com.zhixun.user.config.JwtProperties;
import com.zhixun.user.exception.UserResultCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DefaultRedisScript<String> CONSUME_SCRIPT =
            new DefaultRedisScript<>(
                    "local value = redis.call('get', KEYS[1]); "
                            + "if value then redis.call('del', KEYS[1]); end; "
                            + "return value;",
                    String.class
            );

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;

    public RefreshTokenService(
            StringRedisTemplate redisTemplate,
            JwtProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public RefreshTokenValue issue(Long userId) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        redisTemplate.opsForValue().set(
                redisKey(token),
                userId.toString(),
                properties.refreshTokenTtl()
        );
        return new RefreshTokenValue(token, properties.refreshTokenTtl().toSeconds());
    }

    public Long consume(String token) {
        String userId = redisTemplate.execute(
                CONSUME_SCRIPT,
                List.of(redisKey(token))
        );
        if (userId == null) {
            throw new BusinessException(UserResultCode.REFRESH_TOKEN_INVALID);
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException exception) {
            throw new BusinessException(UserResultCode.REFRESH_TOKEN_INVALID);
        }
    }

    public void revoke(String token) {
        redisTemplate.delete(redisKey(token));
    }

    private String redisKey(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
