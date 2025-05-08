/**
 * 인증 서비스의 기본 예외 클래스
 * 모든 사용자 정의 예외는 이 클래스를 상속받아 구현
 */
package com.nsmm.esg.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AuthException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    protected AuthException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
} 