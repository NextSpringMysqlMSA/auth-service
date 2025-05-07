/**
 * 입력값이 유효하지 않을 때 발생하는 예외
 */
package com.nsmm.esg.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends AuthException {
    private static final String ERROR_CODE = "AUTH-004";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvalidInputException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public InvalidInputException(String field, String message) {
        super(String.format("입력값이 유효하지 않습니다. %s: %s", field, message), STATUS, ERROR_CODE);
    }
} 