/**
 * 인증 정보(비밀번호 등)가 유효하지 않을 때 발생하는 예외
 */
package com.nsmm.esg.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AuthException {
    private static final String ERROR_CODE = "AUTH-002";
    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public InvalidCredentialsException() {
        super("인증 정보가 유효하지 않습니다.", STATUS, ERROR_CODE);
    }

    public InvalidCredentialsException(String message) {
        super(message, STATUS, ERROR_CODE);
    }
} 