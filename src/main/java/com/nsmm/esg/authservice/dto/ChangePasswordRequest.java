package com.nsmm.esg.authservice.dto;

import lombok.Getter;

@Getter
public class ChangePasswordRequest {
    private String currentPassword; // 지금 비밀번호
    private String newPassword; // 새로운 비밀번호
    private String confirmPassword; // 새로운 비밀번호 확인
}
