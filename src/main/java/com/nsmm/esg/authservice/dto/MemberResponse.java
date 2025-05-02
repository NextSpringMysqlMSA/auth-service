package com.nsmm.esg.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberResponse {

    private Long memberId;
    private String name;
    private String email;
    private String phoneNumber;
    private String companyName;
    private String position;
    private String profileImageUrl;

}