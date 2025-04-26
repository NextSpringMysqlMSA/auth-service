package com.nsmm.esg.authservice.controller;

import com.nsmm.esg.authservice.dto.LoginRequest;
import com.nsmm.esg.authservice.dto.LoginResponse;
import com.nsmm.esg.authservice.dto.RegisterRequest;
import com.nsmm.esg.authservice.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @PostMapping("register")
    public String register(@RequestBody RegisterRequest request) {
        // 클라이언트로부터 회원가입 요청을 받음 (이름, 이메일, 비밀번호 등 포함)
        // MemberService를 통해 회원가입 로직 수행 (중복 체크 및 저장)
        // 성공 시 "success" 문자열 반환
        memberService.register(request);
        return "success";
    }
    //------------------------------------------------------------------------------------------------------

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // 클라이언트로부터 로그인 요청을 받음 (이메일, 비밀번호 포함)
        // MemberService에서 이메일 존재 여부 및 비밀번호 일치 확인 후 JWT 생성
        // 생성된 JWT 토큰을 JSON 형태로 반환 ({"token": "Bearer ey..."} 형태)
        String token = memberService.login(request);
        return new LoginResponse(token);
    }
    //------------------------------------------------------------------------------------------------------


}
