/**
 * 이미 존재하는 리소스(이메일 등)가 있을 때 발생하는 예외
 */
package com.nsmm.esg.authservice.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends AuthException {
    private static final String ERROR_CODE = "AUTH-003";
    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public DuplicateResourceException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public DuplicateResourceException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s가 이미 %s '%s'로 존재합니다.", resourceName, fieldName, fieldValue),
                STATUS, ERROR_CODE);
    }
} 