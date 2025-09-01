
# 회원가입 → 로그인 → 내 정보 조회 API 흐름

이 흐름은 사용자의 **회원가입 → 로그인 → 내 정보 확인**까지의 인증 과정을 나타냅니다.  
JWT 기반 인증 시스템을 통해 보안이 적용된 사용자 식별 및 정보 조회가 가능합니다.

---
 
## 기능 요약

| 단계 | 설명 |
|------|------|
| 회원가입 | 사용자로부터 입력받은 정보로 회원 계정 생성 |
| 로그인 | 이메일/비밀번호 기반 인증 → JWT 토큰 발급 |
| 내 정보 조회 | JWT 토큰을 통해 인증 후, 사용자 정보 조회 API 요청 |

---

## 인증 방식

- 로그인 성공 시 `JWT 토큰`이 발급되며, 프론트엔드는 이를 `HttpOnly 쿠키` 또는 `Authorization 헤더`에 저장
- 모든 사용자 정보 요청은 `X-MEMBER-ID` 또는 토큰 기반 인증 확인 과정을 거침

---

## 흐름도 (Mermaid)

```mermaid
flowchart TD
    %% 회원가입
    start((Start))
    start --> regReq[/회원가입 요청/]
    regReq --> regProc[회원가입 처리]
    regProc --> regResp>가입 완료 응답]
    regResp --> loginReq[/로그인 요청/]
    loginReq --> loginProc[로그인 처리]
    loginProc --> loginSuccess{로그인 성공?}

    %% 로그인 실패 (왼쪽으로 분기)
    loginSuccess -- 아니오 --> loginFail[로그인 실패 메시지]
    loginFail --> end1((End))

    %% 로그인 성공 (오른쪽으로 수평 이동 후 아래로 진행)
    loginSuccess -- 예 --> jwt>JWT 토큰 발급]
    jwt --> meReq[/내 정보 조회 요청/]
    meReq --> authCheck[사용자 인증 확인]
    authCheck --> isAuthed{인증됨?}

    isAuthed -- 예 --> infoQuery[회원 정보 조회]
    infoQuery --> infoResp>회원 정보 반환]
    infoResp --> end2((End))

    isAuthed -- 아니오 --> authFail[401 인증 오류 반환]
    authFail --> end3((End))

    %% 색상 정의
    classDef forest fill:#e6f4ea,stroke:#2e7d32,stroke-width:1.5px,color:#2e7d32;
    classDef terminal fill:#d0f0c0,stroke:#1b5e20,color:#1b5e20;
    classDef error fill:#fdecea,stroke:#c62828,color:#c62828;

    %% 클래스 적용
    class start,end1,end2,end3 terminal;
    class regReq,regProc,regResp,loginReq,loginProc,loginSuccess,jwt,meReq,authCheck,isAuthed,infoQuery,infoResp forest;
    class loginFail,authFail error;
````

---

## 기술 스택

* **Spring Boot + JWT 인증**
* 회원가입/로그인: `AuthController`, `AuthService`
* 내 정보 조회: `/auth/me` API
* 인증 필터: `JwtAuthenticationFilter` → `X-MEMBER-ID` 주입

---

##  개발 포인트

* `IllegalArgumentException` 기반의 예외 처리 → 프론트에서 toast로 메시지 노출
* JWT 토큰 인증 필터에서 `memberId` 추출 후 `X-MEMBER-ID` 헤더로 각 서비스 전달
* 인증 실패 및 로그인 실패 메시지를 명확하게 구분하여 반환

