## 🌿 회원가입 → 로그인 → 내 정보 조회 흐름 

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
