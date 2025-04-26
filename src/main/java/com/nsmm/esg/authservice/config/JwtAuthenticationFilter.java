package com.nsmm.esg.authservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// 요청마다 한 번 실행되는 필터 (JWT 인증용)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 토큰 유효성 검사 및 사용자 정보 추출 기능 제공
    private final JwtTokenProvider jwtTokenProvider;

    // 생성자: 필터 생성 시 JwtTokenProvider 주입
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    //------------------------------------------------------------------------------------------------------

    /**
     * 요청이 들어올 때마다 실행되는 필터 로직
     * 1. Authorization 헤더에서 JWT 추출
     * 2. 토큰 유효성 검사 후 사용자 인증 정보 설정
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 요청 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 존재하고 유효하면 인증 처리
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 사용자 이메일 추출
            String email = jwtTokenProvider.getEmail(token);

            // 4. 인증 객체 생성 (권한 정보는 없음)
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

            // 5. 현재 요청의 보안 컨텍스트에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 다음 필터 또는 컨트롤러로 요청 전달
        filterChain.doFilter(request, response);
    }
    //------------------------------------------------------------------------------------------------------

    /**
     * HTTP 요청의 Authorization 헤더에서 Bearer 토큰 추출
     * @param request HTTP 요청 객체
     * @return 순수 JWT 토큰 문자열 (Bearer 제거)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // "Bearer "로 시작하면 토큰 값만 추출
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후 부분 반환
        }

        // 유효한 Authorization 헤더 없으면 null 반환
        return null;
    }
    //------------------------------------------------------------------------------------------------------
}
