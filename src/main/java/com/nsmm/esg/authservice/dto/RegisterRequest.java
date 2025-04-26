package com.nsmm.esg.authservice.dto;

import lombok.Getter;

@Getter
public class RegisterRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String companyName;
    private String position;
    private String password;
}
