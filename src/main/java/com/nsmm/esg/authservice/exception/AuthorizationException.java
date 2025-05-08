/**
 * 사용자가 해당 작업을 수행할 권한이 없을 때 발생하는 예외
 */
package com.nsmm.esg.authservice.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends AuthException {
    private static final String ERROR_CODE = "AUTH-006";
    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

    public AuthorizationException() {
        super("이 작업을 수행할 권한이 없습니다.", STATUS, ERROR_CODE);
    }

    public AuthorizationException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public AuthorizationException(String resource, Long resourceId) {
        super(String.format("리소스 %s (ID: %d)에 접근할 권한이 없습니다.", resource, resourceId), STATUS, ERROR_CODE);
    }
} 