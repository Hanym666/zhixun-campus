package com.zhixun.item.security;

import com.zhixun.item.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class JwtVerifier {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtVerifier(JwtProperties properties) {
        this.properties = properties;
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public AuthenticatedUser verify(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<?> roleClaims = claims.get("roles", List.class);
        Set<String> roles = new LinkedHashSet<>();
        if (roleClaims != null) {
            roleClaims.stream().map(String::valueOf).forEach(roles::add);
        }
        return new AuthenticatedUser(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                Set.copyOf(roles)
        );
    }
}
