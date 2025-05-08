/**
 * 회원 관련 API 컨트롤러
 * - 회원가입, 로그인, 회원정보 조회, 비밀번호 변경 등 회원 관련 엔드포인트 제공
 */
package com.nsmm.esg.authservice.controller;

import com.nsmm.esg.authservice.dto.*;
import com.nsmm.esg.authservice.exception.AuthorizationException;
import com.nsmm.esg.authservice.exception.InvalidInputException;
import com.nsmm.esg.authservice.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 인증된 사용자 ID 추출 공통 메서드
     * @throws AuthorizationException 인증 정보가 없거나 유효하지 않은 경우
     */
    private Long getCurrentMemberId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AuthorizationException("로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long id) return id;
        if (principal instanceof String str) return Long.parseLong(str);

        throw new AuthorizationException("인증 정보가 유효하지 않습니다.");
    }

    /**
     * 내 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(HttpServletRequest request) {
        Long memberId = getCurrentMemberId();
        log.info("내 정보 조회 요청: 회원ID={}, 요청IP={}", memberId, request.getRemoteAddr());
        
        MemberResponse response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입 API
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest servletRequest) {
        log.info("회원가입 요청: 이메일={}, 요청IP={}", request.getEmail(), servletRequest.getRemoteAddr());
        
        memberService.register(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        log.info("로그인 요청: 이메일={}, 요청IP={}", request.getEmail(), servletRequest.getRemoteAddr());
        
        String token = memberService.login(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    /**
     * 비밀번호 변경 API
     */
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest servletRequest) {
        Long memberId = getCurrentMemberId();
        log.info("비밀번호 변경 요청: 회원ID={}, 요청IP={}", memberId, servletRequest.getRemoteAddr());
        
        memberService.changePassword(memberId, request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    /**
     * 프로필 이미지 업로드 API
     */
    @PutMapping("/profile-image")
    public ResponseEntity<String> updateProfileImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest servletRequest) {
        Long memberId = getCurrentMemberId();
        log.info("프로필 이미지 업로드 요청: 회원ID={}, 파일크기={}KB, 요청IP={}", 
                memberId, file.getSize() / 1024, servletRequest.getRemoteAddr());
        
        if (file.isEmpty()) {
            throw new InvalidInputException("file", "파일이 비어있습니다.");
        }
        
        String imageUrl = memberService.updateProfileImage(memberId, file);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * 프로필 이미지 URL 조회 API
     */
    @GetMapping("/profile-image")
    public ResponseEntity<String> getProfileImageUrl(HttpServletRequest servletRequest) {
        Long memberId = getCurrentMemberId();
        log.debug("프로필 이미지 URL 조회 요청: 회원ID={}, 요청IP={}", memberId, servletRequest.getRemoteAddr());
        
        String imageUrl = memberService.getProfileImageUrl(memberId);
        return ResponseEntity.ok(imageUrl);
    }
}
