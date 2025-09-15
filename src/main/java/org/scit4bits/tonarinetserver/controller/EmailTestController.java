package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import org.scit4bits.tonarinetserver.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이메일 전송 테스트를 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    /**
     * 환영 이메일 전송을 테스트합니다.
     * @param lang 언어 (기본값: "ko")
     * @param email 수신자 이메일
     * @param username 사용자 이름
     * @return 이메일 전송 결과 메시지
     */
    @GetMapping("/email/welcome")
    public String testWelcomeEmail(
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam String email,
            @RequestParam String username) {

        // 환영 이메일 전송 서비스 호출
        emailService.sendWelcomeEmail(lang, email, username);
        return "Welcome email sent to " + email + " in " + lang;
    }

    /**
     * 인증 이메일 전송을 테스트합니다.
     * @param lang 언어 (기본값: "ko")
     * @param email 수신자 이메일
     * @param username 사용자 이름
     * @param code 인증 코드 (기본값: "123456")
     * @return 이메일 전송 결과 메시지
     */
    @GetMapping("/email/verification")
    public String testVerificationEmail(
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(defaultValue = "123456") String code) {

        // 인증 이메일 전송 서비스 호출
        emailService.sendVerificationEmail(lang, email, username, code);
        return "Verification email sent to " + email + " in " + lang;
    }

    /**
     * 비밀번호 재설정 이메일 전송을 테스트합니다.
     * @param lang 언어 (기본값: "ko")
     * @param email 수신자 이메일
     * @param username 사용자 이름
     * @param resetLink 비밀번호 재설정 링크 (기본값: "https://tonarinet.com/reset")
     * @return 이메일 전송 결과 메시지
     */
    @GetMapping("/email/password-reset")
    public String testPasswordResetEmail(
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(defaultValue = "https://tonarinet.com/reset") String resetLink) {

        // 비밀번호 재설정 이메일 전송 서비스 호출
        emailService.sendPasswordResetEmail(lang, email, username, resetLink);
        return "Password reset email sent to " + email + " in " + lang;
    }
}
