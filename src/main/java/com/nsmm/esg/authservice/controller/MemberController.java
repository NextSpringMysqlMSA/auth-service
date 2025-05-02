package com.nsmm.esg.authservice.controller;

import com.nsmm.esg.authservice.dto.*;
import com.nsmm.esg.authservice.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 인증된 사용자 ID 추출 공통 메서드
    private Long getCurrentMemberId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long id) return id;
        if (principal instanceof String str) return Long.parseLong(str);

        throw new RuntimeException("인증 정보가 유효하지 않습니다.");
    }

    // ========================= [1] 내 정보 조회 =========================
    @GetMapping("/me")
    public MemberResponse getMyInfo() {
        return memberService.getMemberInfo(getCurrentMemberId());
    }

    // ========================= [2] 회원가입 =========================
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        memberService.register(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // ========================= [3] 로그인 =========================
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = memberService.login(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // ========================= [4] 비밀번호 변경 =========================
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        memberService.changePassword(getCurrentMemberId(), request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    // ========================= [5] 프로필 이미지 변경 =========================
    @PutMapping("/profile-image")
    public ResponseEntity<String> updateProfileImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어있습니다.");
        }
        String imageUrl = memberService.updateProfileImage(getCurrentMemberId(), file);
        return ResponseEntity.ok(imageUrl);
    }

    @GetMapping("/profile-image")
    public ResponseEntity<String> getProfileImageUrl() {
        Long memberId = getCurrentMemberId(); // 인증된 사용자
        String imageUrl = memberService.getProfileImageUrl(memberId);
        return ResponseEntity.ok(imageUrl); // 예: "/images/abc.jpg"
    }

}
