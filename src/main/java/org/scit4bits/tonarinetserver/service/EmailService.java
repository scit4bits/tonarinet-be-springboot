package org.scit4bits.tonarinetserver.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.enums.EmailLanguage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 이메일 발송 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailMessageProvider messageProvider;

    @Value("${spring.mail.sender}")
    private String sender;

    /**
     * 이메일을 발송합니다.
     * @param lang 언어 코드
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param body 이메일 본문 (HTML)
     */
    public void sendEmail(String lang, String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true는 HTML 메일임을 나타냅니다.

            mailSender.send(message);
            log.info("이메일 발송 성공: {} (언어: {})", to, lang);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", to, e);
        }
    }

    /**
     * 이메일 템플릿 파일을 로드합니다.
     * @param templateName 템플릿 이름
     * @return 템플릿 파일의 내용
     */
    private String loadTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/" + templateName + ".html");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("이메일 템플릿 로드 실패: {}", templateName, e);
            return getBasicTemplate(); // 템플릿 로드 실패 시 기본 템플릿 사용
        }
    }

    /**
     * 템플릿 로드 실패 시 사용할 기본 HTML 템플릿을 반환합니다.
     * @return 기본 템플릿 문자열
     */
    private String getBasicTemplate() {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><title>%s</title></head>
                <body>
                    <h1>🌟 Tonarinet</h1>
                    <h2>%s</h2>
                    <div>%s</div>
                    <p>© 2025 Tonarinet. All rights reserved.</p>
                </body>
                </html>
                """;
    }

    /**
     * 환영 이메일을 발송합니다.
     * @param lang 언어 코드
     * @param to 수신자 이메일 주소
     * @param username 사용자 이름
     */
    public void sendWelcomeEmail(String lang, String to, String username) {
        EmailLanguage language = EmailLanguage.fromCode(lang);

        String subject = messageProvider.getMessage(language, "welcome.subject");
        String template = loadTemplate("welcome");

        String htmlContent = String.format(template,
                language.getCode(), // lang 속성
                subject, // title
                subject, // header
                messageProvider.getMessage(language, "welcome.greeting", username),
                messageProvider.getMessage(language, "welcome.content"),
                messageProvider.getMessage(language, "welcome.features"),
                messageProvider.getMessage(language, "welcome.feature.community"),
                messageProvider.getMessage(language, "welcome.feature.chat"),
                messageProvider.getMessage(language, "welcome.feature.profile"),
                messageProvider.getMessage(language, "welcome.feature.notifications"),
                messageProvider.getMessage(language, "welcome.contact"),
                messageProvider.getMessage(language, "welcome.closing"),
                messageProvider.getMessage(language, "footer.disclaimer"),
                messageProvider.getMessage(language, "footer.copyright"),
                messageProvider.getMessage(language, "footer.support"));

        sendEmail(lang, to, subject, htmlContent);
    }

    /**
     * 이메일 인증 코드를 발송합니다.
     * @param lang 언어 코드
     * @param to 수신자 이메일 주소
     * @param username 사용자 이름
     * @param verificationCode 인증 코드
     */
    public void sendVerificationEmail(String lang, String to, String username, String verificationCode) {
        EmailLanguage language = EmailLanguage.fromCode(lang);

        String subject = messageProvider.getMessage(language, "verification.subject");
        String template = loadTemplate("verification");

        String htmlContent = String.format(template,
                language.getCode(), // lang 속성
                subject, // title
                subject, // header
                messageProvider.getMessage(language, "verification.greeting", username),
                messageProvider.getMessage(language, "verification.content"),
                verificationCode, // 인증 코드
                messageProvider.getMessage(language, "verification.warning.title"),
                messageProvider.getMessage(language, "verification.warning.expire"),
                messageProvider.getMessage(language, "verification.warning.private"),
                messageProvider.getMessage(language, "verification.warning.ignore"),
                messageProvider.getMessage(language, "footer.disclaimer"),
                messageProvider.getMessage(language, "footer.copyright"),
                messageProvider.getMessage(language, "footer.support"));

        sendEmail(lang, to, subject, htmlContent);
    }

    /**
     * 비밀번호 재설정 이메일을 발송합니다.
     * @param lang 언어 코드
     * @param to 수신자 이메일 주소
     * @param username 사용자 이름
     * @param resetLink 비밀번호 재설정 링크
     */
    public void sendPasswordResetEmail(String lang, String to, String username, String resetLink) {
        EmailLanguage language = EmailLanguage.fromCode(lang);

        String subject = messageProvider.getMessage(language, "password.reset.subject");
        String template = loadTemplate("password-reset");

        String htmlContent = String.format(template,
                language.getCode(), // lang 속성
                subject, // title
                subject, // header
                messageProvider.getMessage(language, "password.reset.greeting", username),
                messageProvider.getMessage(language, "password.reset.content"),
                messageProvider.getMessage(language, "password.reset.instruction"),
                resetLink, // 버튼에 사용될 재설정 링크
                messageProvider.getMessage(language, "password.reset.button"),
                messageProvider.getMessage(language, "password.reset.security.title"),
                messageProvider.getMessage(language, "password.reset.security.expire"),
                messageProvider.getMessage(language, "password.reset.security.ignore"),
                messageProvider.getMessage(language, "password.reset.security.copy"),
                resetLink, // 복사-붙여넣기용 재설정 링크
                messageProvider.getMessage(language, "footer.disclaimer"),
                messageProvider.getMessage(language, "footer.copyright"),
                messageProvider.getMessage(language, "footer.support"));

        sendEmail(lang, to, subject, htmlContent);
    }
}
