package com.nsmm.esg.authservice.config;

import jakarta.ws.rs.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    //------------------------------------------------------------------------------------------------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 기본 설정 적용 (BeanConfig에서 정의된 CORS 설정 사용)
                .cors(Customizer.withDefaults())

                // CSRF 비활성화 (JWT 기반 Stateless 방식이므로 필요 없음)
                .csrf(csrf -> csrf.disable())

                // 기본 로그인 폼/HTTP Basic 인증 비활성화 (JWT 방식 사용하므로)
                .httpBasic(httpBasic -> httpBasic.disable())

                // 세션 사용 안 함 - 완전 Stateless 방식
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청 URL 별 보안 설정
                .authorizeHttpRequests(auth -> auth
                        // 프리플라이트 요청 허용 (CORS 대응을 위해 OPTIONS 메서드 허용)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증 필요 없는 경로 설정 (/auth/**는 로그인, 회원가입 등 허용)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        // 그 외 모든 요청은 인증 필요 (JWT 필터를 통해 인증됨)
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 등록 (UsernamePasswordAuthenticationFilter 앞에 실행됨)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build(); // SecurityFilterChain 반환
    }
    //------------------------------------------------------------------------------------------------------
}



