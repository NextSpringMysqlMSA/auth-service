package com.nsmm.esg.authservice.config;

import org.springframework.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * - HTTP 보안 구성 (요청별 접근 제어, JWT 필터 등록 등)
 * - Stateless 기반 API 서버에 최적화된 설정 사용
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT 토큰 유효성 검사 및 사용자 인증 처리에 사용되는 Provider
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * SecurityFilterChain 설정 (Spring Security 5 이상에서 사용)
     * - JWT 기반 인증 및 인가를 위한 보안 필터 체인 구성
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 활성화
                // 기본적으로 WebMvcConfigurer에 정의한 cors 설정을 적용
                .cors(Customizer.withDefaults())

                // CSRF 보호 비활성화
                // JWT 기반 API 서버는 세션을 사용하지 않기 때문에 CSRF 보호 필요 없음
                .csrf(csrf -> csrf.disable())

                // HTTP Basic 인증 방식 비활성화 (브라우저 팝업 방식 로그인 창 방지)
                .httpBasic(httpBasic -> httpBasic.disable())

                // 세션 생성 정책 설정
                // Spring Security가 세션을 생성하거나 사용하지 않도록 설정 (JWT 기반 인증을 위해 필수)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 제어 설정
                .authorizeHttpRequests(auth -> auth
                        // CORS 사전 요청(OPTIONS 메서드)은 인증 없이 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증이 필요 없는 공용 API 경로
                        .requestMatchers("/auth/**").permitAll()        // 로그인, 회원가입 등
                        .requestMatchers("/images/**").permitAll()      // 정적 이미지 접근 허용
                        .requestMatchers("/actuator/**").permitAll()    // Actuator 엔드포인트 접근 허용

                        // 위에서 명시한 경로 외에는 모두 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 등록
                // UsernamePasswordAuthenticationFilter 앞에 위치시켜 요청마다 JWT 검사 우선 실행
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        // 최종적으로 구성된 필터 체인 반환
        return http.build();
    }
}
