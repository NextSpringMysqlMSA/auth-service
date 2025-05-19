/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
package com.nsmm.esg.authservice.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AuthException {
    private static final String ERROR_CODE = "AUTH-001";
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public UserNotFoundException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public UserNotFoundException(Long userId) {
        super("사용자 ID가 " + userId + "인 사용자를 찾을 수 없습니다.", STATUS, ERROR_CODE);
    }

    public UserNotFoundException(String email, String reason) {
        super("이메일이 " + email + "인 사용자를 찾을 수 없습니다. 이유: " + reason, STATUS, ERROR_CODE);
    }
}