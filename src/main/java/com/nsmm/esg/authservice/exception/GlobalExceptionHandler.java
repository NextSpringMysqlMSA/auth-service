/**
 * 전역 예외 처리 핸들러
 * - 애플리케이션에서 발생하는 모든 예외를 일관된 형식으로 클라이언트에게 반환
 * - 각 예외 유형별로 적절한 HTTP 상태 코드와 응답 형식을 제공
 */
package com.nsmm.esg.authservice.exception;

import com.nsmm.esg.authservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 사용자 정의 AuthException 계열 예외 처리
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, HttpServletRequest request) {
        log.error("AuthException 발생: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.of(
                ex.getErrorCode(), 
                ex.getMessage(), 
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, ex.getStatus());
    }

    // 폼 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("검증 예외 발생: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse response = ErrorResponse.of(
                "AUTH-400", 
                "입력값 검증에 실패했습니다.", 
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    // 파일 업로드 크기 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.error("파일 크기 초과 예외 발생: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.of(
                "AUTH-413", 
                "파일 크기가 허용된 최대 크기를 초과했습니다.", 
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // 접근 권한 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.error("접근 권한 예외 발생: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.of(
                "AUTH-403", 
                "접근 권한이 없습니다.", 
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 데이터 무결성 위반 예외 처리 (DB 제약조건 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("데이터 무결성 위반 예외 발생: {}", ex.getMessage(), ex);
        
        String message = "데이터베이스 제약조건 위반이 발생했습니다.";
        if (ex.getMessage().contains("Duplicate entry") || ex.getMessage().contains("unique constraint")) {
            message = "이미 존재하는 데이터입니다.";
        }
        
        ErrorResponse response = ErrorResponse.of(
                "AUTH-409", 
                message, 
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 예상치 못한 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("예상치 못한 예외 발생: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.of(
                "AUTH-500", 
                "서버 오류가 발생했습니다.", 
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
