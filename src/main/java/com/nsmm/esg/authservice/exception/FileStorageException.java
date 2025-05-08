/**
 * 파일 저장 관련 예외 발생 시 사용하는 예외 클래스
 */
package com.nsmm.esg.authservice.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends AuthException {
    private static final String ERROR_CODE = "AUTH-005";
    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public FileStorageException(String message) {
        super(message, STATUS, ERROR_CODE);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, STATUS, ERROR_CODE);
        initCause(cause);
    }
} 