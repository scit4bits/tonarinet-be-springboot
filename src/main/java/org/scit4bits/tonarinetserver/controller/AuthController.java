package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.*;
import org.scit4bits.tonarinetserver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API를 처리하는 컨트롤러
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자 회원가입을 처리합니다.
     * @param body 회원가입 요청 정보
     * @return 회원가입 성공 시 200 OK, 실패 시 400 Bad Request
     */
    @PostMapping("/signup")
    public ResponseEntity<SimpleResponse> userSignUp(@RequestBody SignUpRequest body) {
        // 사용자 회원가입 서비스 호출
        if (authService.userSignUp(body)) {
            return ResponseEntity.ok(new SimpleResponse("User created successfully"));
        } else {
            return ResponseEntity.status(400).body(new SimpleResponse("User creation failed"));
        }
    }

    /**
     * 이메일 중복 여부를 확인합니다.
     * @param email 확인할 이메일
     * @return 사용 가능한 이메일이면 true, 아니면 false
     */
    @GetMapping("/emaildupcheck")
    public ResponseEntity<Boolean> getEmailDupCheck(@RequestParam("email") String email) {
        // 이메일 사용 가능 여부 확인 서비스 호출
        boolean isAvailable = authService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * 이메일과 비밀번호를 사용한 로그인을 처리합니다.
     * @param body 로그인 요청 정보 (이메일, 비밀번호)
     * @return 로그인 성공 시 액세스 토큰과 함께 200 OK, 실패 시 400 Bad Request
     */
    @PostMapping("/signin/email")
    public ResponseEntity<SimpleResponse> userSignInEmail(@RequestBody SignInEmailRequest body) {
        try {
            // 이메일/비밀번호 기반 로그인 서비스 호출
            String accessToken = authService.signInWithPassword(body.getEmail(), body.getPassword());
            return ResponseEntity.ok(new SimpleResponse("Login Successful", accessToken));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new SimpleResponse("User sign in failed"));
        }
    }

    /**
     * OAuth를 사용한 로그인을 처리합니다.
     * @param body OAuth 로그인 요청 정보 (provider, oauthId)
     * @return 로그인 성공 시 액세스 토큰과 함께 200 OK, 실패 시 400 Bad Request
     */
    @PostMapping("/signin/oauth")
    public ResponseEntity<SimpleResponse> userSignInOAuth(@RequestBody SignInOAuthRequest body) {
        try {
            // OAuth 기반 로그인 서비스 호출
            String accessToken = authService.signInWithOAuth(body.getProvider(), body.getOauthid());
            return ResponseEntity.ok(new SimpleResponse("Login Successful", accessToken));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new SimpleResponse("User sign in failed"));
        }
    }

    /**
     * OAuth 인증을 위한 상태(state) 값을 생성합니다.
     * @return 생성된 상태 값을 포함한 응답
     */
    @GetMapping("/generateState")
    public ResponseEntity<GenerateStateResponse> getGenerateState() {
        // 상태 값 생성 서비스 호출
        GenerateStateResponse response = authService.generateState();
        return ResponseEntity.ok(response);
    }

    /**
     * Line OAuth 인증 결과를 확인합니다.
     * @param code 인증 코드
     * @param state 상태 값
     * @return 인증 확인 응답
     */
    @GetMapping("/line/check")
    public ResponseEntity<AuthCheckResponse> getLineCheck(@RequestParam("code") String code,
                                                          @RequestParam("state") String state) {
        // Line 인증 확인 서비스 호출
        AuthCheckResponse response = authService.getLineCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Google OAuth 인증 결과를 확인합니다.
     * @param code 인증 코드
     * @param state 상태 값
     * @return 인증 확인 응답
     */
    @GetMapping("/google/check")
    public ResponseEntity<AuthCheckResponse> getGoogleCheck(@RequestParam("code") String code,
                                                            @RequestParam("state") String state) {
        // Google 인증 확인 서비스 호출
        AuthCheckResponse response = authService.getGoogleCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Kakao OAuth 인증 결과를 확인합니다.
     * @param code 인증 코드
     * @param state 상태 값
     * @return 인증 확인 응답
     */
    @GetMapping("/kakao/check")
    public ResponseEntity<AuthCheckResponse> getKakaoCheck(@RequestParam("code") String code,
                                                           @RequestParam("state") String state) {
        // Kakao 인증 확인 서비스 호출
        AuthCheckResponse response = authService.getKakaoCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * 비밀번호 찾기 이메일 전송을 요청합니다.
     * @param email 사용자 이메일
     * @return 이메일 전송 결과 메시지
     */
    @GetMapping("/forgot-password")
    public ResponseEntity<String> getForgotPassword(@RequestParam("email") String email) {
        try {
            // 비밀번호 재설정 이메일 전송 서비스 호출
            authService.sendForgotPasswordEmail(email);
            return ResponseEntity.ok("Password reset email sent if the email exists");
        } catch (Exception e) {
            log.error("Error in forgot password process: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    /**
     * 비밀번호 재설정 토큰의 유효성을 검사합니다.
     * @param token 재설정 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Boolean> validateResetToken(@RequestParam("token") String token) {
        // 토큰 유효성 검사 서비스 호출
        boolean isValid = authService.validateResetToken(token);
        if (isValid) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(400).body(false);
        }
    }

    /**
     * 비밀번호를 재설정합니다.
     * @param body 비밀번호 재설정 요청 정보 (토큰, 새 비밀번호)
     * @return 비밀번호 재설정 결과 메시지
     */
    @PostMapping("/reset-password")
    public ResponseEntity<SimpleResponse> postResetPassword(@RequestBody PasswordResetRequestDTO body) {
        try {
            // 비밀번호 재설정 서비스 호출
            boolean result = authService.resetPassword(body.getToken(), body.getNewPassword());
            if (result) {
                return ResponseEntity.ok(new SimpleResponse("Password has been reset successfully"));
            } else {
                return ResponseEntity.status(400).body(new SimpleResponse("Invalid or expired token"));
            }
        } catch (Exception e) {
            log.error("Error in reset password process: {}", e.getMessage());
            return ResponseEntity.status(500).body(new SimpleResponse("Internal server error"));
        }
    }

}
