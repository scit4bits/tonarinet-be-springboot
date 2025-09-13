package org.scit4bits.tonarinetserver.controller;

import org.scit4bits.tonarinetserver.dto.AuthCheckResponse;
import org.scit4bits.tonarinetserver.dto.GenerateStateResponse;
import org.scit4bits.tonarinetserver.dto.PasswordResetRequestDTO;
import org.scit4bits.tonarinetserver.dto.SignInEmailRequest;
import org.scit4bits.tonarinetserver.dto.SignInOAuthRequest;
import org.scit4bits.tonarinetserver.dto.SignUpRequest;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SimpleResponse> userSignUp(@RequestBody SignUpRequest body) {
        if (authService.userSignUp(body)) {
            return ResponseEntity.ok(new SimpleResponse("User created successfully"));
        } else {
            return ResponseEntity.status(400).body(new SimpleResponse("User creation failed"));
        }
    }

    @GetMapping("/emaildupcheck")
    public ResponseEntity<Boolean> getEmailDupCheck(@RequestParam("email") String email) {
        boolean isAvailable = authService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping("/signin/email")
    public ResponseEntity<SimpleResponse> userSignInEmail(@RequestBody SignInEmailRequest body) {
        try {
            String accessToken = authService.signInWithPassword(body.getEmail(), body.getPassword());
            return ResponseEntity.ok(new SimpleResponse("Login Successful", accessToken));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new SimpleResponse("User sign in failed"));
        }
    }

    @PostMapping("/signin/oauth")
    public ResponseEntity<SimpleResponse> userSignInOAuth(@RequestBody SignInOAuthRequest body) {
        try {
            String accessToken = authService.signInWithOAuth(body.getProvider(), body.getOauthid());
            return ResponseEntity.ok(new SimpleResponse("Login Successful", accessToken));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new SimpleResponse("User sign in failed"));
        }
    }

    @GetMapping("/generateState")
    public ResponseEntity<GenerateStateResponse> getGenerateState() {
        GenerateStateResponse response = authService.generateState();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/line/check")
    public ResponseEntity<AuthCheckResponse> getLineCheck(@RequestParam("code") String code,
            @RequestParam("state") String state) {
        AuthCheckResponse response = authService.getLineCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/google/check")
    public ResponseEntity<AuthCheckResponse> getGoogleCheck(@RequestParam("code") String code,
            @RequestParam("state") String state) {
        AuthCheckResponse response = authService.getGoogleCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/kakao/check")
    public ResponseEntity<AuthCheckResponse> getKakaoCheck(@RequestParam("code") String code,
            @RequestParam("state") String state) {
        AuthCheckResponse response = authService.getKakaoCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/forgot-password")
    public ResponseEntity<String> getForgotPassword(@RequestParam("email") String email) {
        try {
            authService.sendForgotPasswordEmail(email);
            return ResponseEntity.ok("Password reset email sent if the email exists");
        } catch (Exception e) {
            log.error("Error in forgot password process: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Boolean> validateResetToken(@RequestParam("token") String token) {
        boolean isValid = authService.validateResetToken(token);
        if (isValid) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(400).body(false);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SimpleResponse> postResetPassword(@RequestBody PasswordResetRequestDTO body) {
        try {
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
