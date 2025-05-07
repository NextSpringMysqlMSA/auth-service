/**
 * 표준화된 에러 응답을 위한 DTO 클래스
 * - 모든 API 오류 응답의 일관성 있는 형식을 제공
 */
package com.nsmm.esg.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final LocalDateTime timestamp;
    private final String message;
    private final String errorCode;
    private final String path;
    private final Map<String, String> errors;
    
    public static ErrorResponse of(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .build();
    }
    
    public static ErrorResponse of(String errorCode, String message, String path, Map<String, String> errors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .errors(errors)
                .build();
    }
} 