package com.zhixun.user.security;

import com.zhixun.user.config.JwtProperties;
import com.zhixun.user.domain.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public JwtToken createAccessToken(SysUser user, List<String> roles) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());

        String token = Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .signWith(secretKey)
                .compact();

        return new JwtToken(token, expiresAt);
    }

    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        List<?> roleClaims = claims.get("roles", List.class);
        Set<String> roles = new LinkedHashSet<>();
        if (roleClaims != null) {
            roleClaims.stream()
                    .map(String::valueOf)
                    .forEach(roles::add);
        }
        return new AuthenticatedUser(userId, username, Set.copyOf(roles));
    }

    public long accessTokenExpiresInSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }
}
