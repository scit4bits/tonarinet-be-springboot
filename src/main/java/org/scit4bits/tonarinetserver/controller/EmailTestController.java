package org.scit4bits.tonarinetserver.controller;

import org.scit4bits.tonarinetserver.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @GetMapping("/email/welcome")
    public String testWelcomeEmail(
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam String email,
            @RequestParam String username) {

        emailService.sendWelcomeEmail(lang, email, username);
        return "Welcome email sent to " + email + " in " + lang;
    }

    @GetMapping("/email/verification")
    public String testVerificationEmail(
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(defaultValue = "123456") String code) {

        emailService.sendVerificationEmail(lang, email, username, code);
        return "Verification email sent to " + email + " in " + lang;
    }

    @GetMapping("/email/password-reset")
    public String testPasswordResetEmail(
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(defaultValue = "https://tonarinet.com/reset") String resetLink) {

        emailService.sendPasswordResetEmail(lang, email, username, resetLink);
        return "Password reset email sent to " + email + " in " + lang;
    }
}