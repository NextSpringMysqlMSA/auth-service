package com.nsmm.esg.authservice.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CSPFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // HTTP 응답 객체로 캐스팅 (헤더 추가를 위해 필요)
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // Content Security Policy (CSP) 헤더 설정:
        // - default-src 'self' : 모든 리소스 기본 출처는 현재 도메인만 허용
        // - script-src 'self' : 스크립트(js)는 현재 도메인만 허용
        // - object-src 'none' : 플래시/자바앱릿 같은 객체 소스는 전부 차단
        httpResp.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; object-src 'none';");

        // 다음 필터 또는 서블릿으로 요청 전달
        chain.doFilter(request, response);
    }
    //------------------------------------------------------------------------------------------------------

}
