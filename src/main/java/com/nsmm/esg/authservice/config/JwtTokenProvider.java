package com.nsmm.esg.authservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스.
 * - 토큰 생성 시: 이메일을 기반으로 JWT 생성.
 * - 토큰 검증 시: 유효성 체크 및 이메일 추출 가능.
 */
@Component
public class JwtTokenProvider {

    // application.properties 또는 config 서버에서 주입되는 비밀키 (Base64 인코딩 가능)
    @Value("${jwt.secret}")
    private String secretKeyEncoded;

    // JWT 서명을 위한 Key 객체 (초기화 후 사용)
    private Key key;

    // 토큰 만료 시간 (시간 단위, 기본값 1시간 예상)
    @Value("${jwt.expiration-hours}")
    private long expirationHours;

    /**
     * 초기화 메서드.
     * - secretKeyEncoded를 기반으로 HMAC-SHA 키 생성.
     */
    @PostConstruct
    protected void init() {
        // 문자열 비밀 키를 바이트 배열로 변환 후 HMAC 키 생성
        this.key = Keys.hmacShaKeyFor(secretKeyEncoded.getBytes());
    }
    //------------------------------------------------------------------------------------------------------

    /**
     * JWT 생성 메서드.
     * - 이메일을 Subject로 사용하여 JWT 발급.
     * - 현재 시간 기준으로 만료 시간 설정.
     *
     * @param email 사용자 이메일 (토큰에 저장)
     * @return Bearer 형식의 JWT 문자열
     */
    public String createToken(String email) {
        Date now = new Date(); // 현재 시간
        Date expiry = new Date(now.getTime() + expirationHours * 60 * 60 * 1000L); // 만료 시간 계산 (밀리초 단위)

        return "Bearer " + Jwts.builder()
                .setClaims(Jwts.claims().setSubject(email)) // 이메일을 Subject로 설정
                .setIssuedAt(now) // 토큰 발급 시간
                .setExpiration(expiry) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘 및 키 설정
                .compact(); // 토큰 생성
    }
    //------------------------------------------------------------------------------------------------------

    /**
     * JWT에서 이메일 추출 메서드.
     *
     * @param token JWT 토큰 문자열
     * @return 토큰에 포함된 이메일 (Subject)
     */
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // 서명 키 설정
                .build()
                .parseClaimsJws(token) // 토큰 파싱
                .getBody()
                .getSubject(); // Subject (email) 반환
    }
    //------------------------------------------------------------------------------------------------------

    /**
     * JWT 유효성 검증 메서드.
     * - 서명 및 만료 시간 체크.
     *
     * @param token 검증할 JWT
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); // 유효한 경우 예외 없음
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 오류, 만료, 포맷 오류 등 발생 시 false 반환
            return false;
        }
    }
    //------------------------------------------------------------------------------------------------------
}
