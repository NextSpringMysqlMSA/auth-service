/**
 * 메트릭 설정 클래스
 * - Prometheus 메트릭 카운터 및 태그 정의
 */
package com.nsmm.esg.authservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(MeterRegistry.class)
public class MetricsConfig {

    private final MeterRegistry registry;

    public MetricsConfig(MeterRegistry registry) {
        this.registry = registry;
    }

    @Bean
    public Counter loginSuccessCounter() {
        return Counter.builder("auth.login.success")
                .description("로그인 성공 수")
                .register(registry);
    }

    @Bean
    public Counter loginFailureCounter() {
        return Counter.builder("auth.login.failure")
                .description("로그인 실패 수")
                .register(registry);
    }
    
    @Bean
    public Counter registerSuccessCounter() {
        return Counter.builder("auth.register.success")
                .description("회원가입 성공 수")
                .register(registry);
    }
    
    @Bean
    public Counter registerFailureCounter() {
        return Counter.builder("auth.register.failure")
                .description("회원가입 실패 수")
                .register(registry);
    }
} 