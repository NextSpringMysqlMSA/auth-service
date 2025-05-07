/**
 * 회원 관리 서비스
 * - 회원가입, 로그인, 인증, 회원정보 관리 등의 비즈니스 로직 담당
 */
package com.nsmm.esg.authservice.service;

import com.nsmm.esg.authservice.config.JwtTokenProvider;
import com.nsmm.esg.authservice.dto.ChangePasswordRequest;
import com.nsmm.esg.authservice.dto.MemberResponse;
import com.nsmm.esg.authservice.entity.Member;
import com.nsmm.esg.authservice.dto.LoginRequest;
import com.nsmm.esg.authservice.dto.RegisterRequest;
import com.nsmm.esg.authservice.exception.*;
import com.nsmm.esg.authservice.repository.MemberRepository;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileStorageService fileStorageService;
    
    // Optional 메트릭 카운터
    private final Optional<Counter> loginSuccessCounter;
    private final Optional<Counter> loginFailureCounter;
    private final Optional<Counter> registerSuccessCounter; 
    private final Optional<Counter> registerFailureCounter;

    @Autowired
    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            FileStorageService fileStorageService,
            @Autowired(required = false) Counter loginSuccessCounter,
            @Autowired(required = false) Counter loginFailureCounter,
            @Autowired(required = false) Counter registerSuccessCounter,
            @Autowired(required = false) Counter registerFailureCounter) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.fileStorageService = fileStorageService;
        this.loginSuccessCounter = Optional.ofNullable(loginSuccessCounter);
        this.loginFailureCounter = Optional.ofNullable(loginFailureCounter);
        this.registerSuccessCounter = Optional.ofNullable(registerSuccessCounter);
        this.registerFailureCounter = Optional.ofNullable(registerFailureCounter);
    }

    /**
     * 메트릭 카운터 증가
     */
    private void incrementCounter(Optional<Counter> counter) {
        counter.ifPresent(Counter::increment);
    }

    /**
     * 회원가입 메서드
     * @param request 회원가입 요청 정보
     */
    @Transactional
    public void register(RegisterRequest request) {
        log.info("회원가입 시도: 이메일={}", request.getEmail());
        
        try {
            // 필수값 검사
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new InvalidInputException("email", "이메일을 입력하세요.");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new InvalidInputException("password", "비밀번호를 입력하세요.");
            }
            if (request.getName() == null || request.getName().isBlank()) {
                throw new InvalidInputException("name", "이름을 입력하세요.");
            }

            // 이메일 형식 유효성 검사
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new InvalidInputException("email", "유효하지 않은 이메일 형식입니다.");
            }

            // 비밀번호 보안 검사
            if (request.getPassword().length() < 8) {
                throw new InvalidInputException("password", "비밀번호는 8자 이상이어야 합니다.");
            }

            // 이메일 중복 체크
            if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new DuplicateResourceException("회원", "이메일", request.getEmail());
            }

            Member member = Member.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .companyName(request.getCompanyName())
                    .position(request.getPosition())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            memberRepository.save(member);
            log.info("회원가입 성공: 이메일={}, 회원ID={}", request.getEmail(), member.getId());
            incrementCounter(registerSuccessCounter);
        } catch (Exception e) {
            log.error("회원가입 실패: 이메일={}, 원인={}", request.getEmail(), e.getMessage(), e);
            incrementCounter(registerFailureCounter);
            throw e;
        }
    }

    /**
     * 로그인 메서드
     * @param request 로그인 요청 정보
     * @return JWT 토큰
     */
    public String login(LoginRequest request) {
        log.info("로그인 시도: 이메일={}", request.getEmail());
        
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new InvalidInputException("email", "이메일을 입력하세요.");
            }

            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new InvalidInputException("password", "비밀번호를 입력하세요.");
            }

            Member member = memberRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(request.getEmail(), "계정이 존재하지 않습니다."));

            if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
                throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
            }

            String token = jwtTokenProvider.createToken(member.getId());
            log.info("로그인 성공: 이메일={}, 회원ID={}", request.getEmail(), member.getId());
            incrementCounter(loginSuccessCounter);
            return token;
        } catch (Exception e) {
            log.error("로그인 실패: 이메일={}, 원인={}", request.getEmail(), e.getMessage(), e);
            incrementCounter(loginFailureCounter);
            throw e;
        }
    }

    /**
     * 회원 정보 조회 메서드
     * @param memberId 회원 ID
     * @return 회원 정보 응답 객체
     */
    public MemberResponse getMemberInfo(Long memberId) {
        log.debug("회원 정보 조회: 회원ID={}", memberId);
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException(memberId));

        return MemberResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .companyName(member.getCompanyName())
                .position(member.getPosition())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    /**
     * 비밀번호 변경 메서드
     * @param memberId 회원 ID
     * @param request 비밀번호 변경 요청 정보
     */
    @Transactional
    public void changePassword(Long memberId, ChangePasswordRequest request) {
        log.info("비밀번호 변경 시도: 회원ID={}", memberId);
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException(memberId));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new InvalidInputException("newPassword", "새 비밀번호는 8자 이상이어야 합니다.");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidInputException("confirmPassword", "새 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 변경
        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
        log.info("비밀번호 변경 성공: 회원ID={}", memberId);
    }

    /**
     * 프로필 이미지 업데이트 메서드
     * @param memberId 회원 ID
     * @param file 업로드된 이미지 파일
     * @return 이미지 URL
     */
    @Transactional
    public String updateProfileImage(Long memberId, MultipartFile file) {
        log.info("프로필 이미지 변경 시도: 회원ID={}", memberId);
        
        try {
            // 파일 저장
            String imageUrl = fileStorageService.store(file);

            // 사용자 조회 및 이미지 경로 업데이트
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new UserNotFoundException(memberId));

            member.updateProfileImageUrl(imageUrl);
            memberRepository.save(member);
            log.info("프로필 이미지 변경 성공: 회원ID={}, 이미지URL={}", memberId, imageUrl);
            return imageUrl;
        } catch (Exception e) {
            log.error("프로필 이미지 변경 실패: 회원ID={}, 원인={}", memberId, e.getMessage(), e);
            throw new FileStorageException("프로필 이미지 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 프로필 이미지 URL 조회 메서드
     * @param memberId 회원 ID
     * @return 이미지 URL
     */
    public String getProfileImageUrl(Long memberId) {
        log.debug("프로필 이미지 URL 조회: 회원ID={}", memberId);
        
        return memberRepository.findById(memberId)
                .map(Member::getProfileImageUrl)
                .orElseThrow(() -> new UserNotFoundException(memberId));
    }
}
