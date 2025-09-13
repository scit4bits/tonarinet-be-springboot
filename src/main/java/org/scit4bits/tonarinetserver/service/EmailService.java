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

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailMessageProvider messageProvider;

    @Value("${spring.mail.sender}")
    private String sender;

    public void sendEmail(String lang, String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {} (language: {})", to, lang);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    private String loadTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/" + templateName + ".html");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templateName, e);
            return getBasicTemplate();
        }
    }

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

    public void sendWelcomeEmail(String lang, String to, String username) {
        EmailLanguage language = EmailLanguage.fromCode(lang);

        String subject = messageProvider.getMessage(language, "welcome.subject");
        String template = loadTemplate("welcome");

        String htmlContent = String.format(template,
                language.getCode(), // lang attribute
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

    public void sendVerificationEmail(String lang, String to, String username, String verificationCode) {
        EmailLanguage language = EmailLanguage.fromCode(lang);

        String subject = messageProvider.getMessage(language, "verification.subject");
        String template = loadTemplate("verification");

        String htmlContent = String.format(template,
                language.getCode(), // lang attribute
                subject, // title
                subject, // header
                messageProvider.getMessage(language, "verification.greeting", username),
                messageProvider.getMessage(language, "verification.content"),
                verificationCode, // verification code
                messageProvider.getMessage(language, "verification.warning.title"),
                messageProvider.getMessage(language, "verification.warning.expire"),
                messageProvider.getMessage(language, "verification.warning.private"),
                messageProvider.getMessage(language, "verification.warning.ignore"),
                messageProvider.getMessage(language, "footer.disclaimer"),
                messageProvider.getMessage(language, "footer.copyright"),
                messageProvider.getMessage(language, "footer.support"));

        sendEmail(lang, to, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String lang, String to, String username, String resetLink) {
        EmailLanguage language = EmailLanguage.fromCode(lang);

        String subject = messageProvider.getMessage(language, "password.reset.subject");
        String template = loadTemplate("password-reset");

        String htmlContent = String.format(template,
                language.getCode(), // lang attribute
                subject, // title
                subject, // header
                messageProvider.getMessage(language, "password.reset.greeting", username),
                messageProvider.getMessage(language, "password.reset.content"),
                messageProvider.getMessage(language, "password.reset.instruction"),
                resetLink, // reset link for button
                messageProvider.getMessage(language, "password.reset.button"),
                messageProvider.getMessage(language, "password.reset.security.title"),
                messageProvider.getMessage(language, "password.reset.security.expire"),
                messageProvider.getMessage(language, "password.reset.security.ignore"),
                messageProvider.getMessage(language, "password.reset.security.copy"),
                resetLink, // reset link for copy section
                messageProvider.getMessage(language, "footer.disclaimer"),
                messageProvider.getMessage(language, "footer.copyright"),
                messageProvider.getMessage(language, "footer.support"));

        sendEmail(lang, to, subject, htmlContent);
    }
}
