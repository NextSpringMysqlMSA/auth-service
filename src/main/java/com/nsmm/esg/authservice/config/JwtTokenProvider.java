package com.nsmm.esg.authservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyEncoded;

    private Key key;

    @Value("${jwt.expiration-hours}")
    private long expirationHours;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyEncoded.getBytes());
    }

    /**
     * JWT 생성
     * @param userId 사용자 고유 ID
     * @return Bearer JWT 토큰 문자열
     */
    public String createToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationHours * 60 * 60 * 1000L);

        return "Bearer " + Jwts.builder()
                .setSubject(String.valueOf(userId)) // ✅ subject에 userId 저장
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 userId 추출
     * @param token JWT 토큰
     * @return userId
     */
    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    /**
     * JWT 유효성 검사
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); // 유효하면 예외 없음
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
