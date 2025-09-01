package org.scit4bits.tonarinetserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;

    @Value("${spring.mail.sender}")
    private String sender;


    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Create neat HTML email template
            String htmlTemplate = createEmailTemplate(subject, body);
            helper.setText(htmlTemplate, true);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    private String createEmailTemplate(String subject, String body) {
        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .email-header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .email-header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 300;
                    }
                    .email-body {
                        padding: 40px 30px;
                        color: #333333;
                    }
                    .email-body h2 {
                        color: #667eea;
                        border-bottom: 2px solid #667eea;
                        padding-bottom: 10px;
                        margin-bottom: 20px;
                    }
                    .email-content {
                        background-color: #f8f9fa;
                        padding: 25px;
                        border-radius: 8px;
                        margin: 20px 0;
                        border-left: 4px solid #667eea;
                    }
                    .email-footer {
                        background-color: #333333;
                        color: #ffffff;
                        padding: 20px 30px;
                        text-align: center;
                        font-size: 14px;
                    }
                    .email-footer a {
                        color: #667eea;
                        text-decoration: none;
                    }
                    .divider {
                        height: 2px;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        margin: 30px 0;
                        border-radius: 1px;
                    }
                    @media (max-width: 600px) {
                        .email-container {
                            margin: 10px;
                            border-radius: 5px;
                        }
                        .email-header, .email-body, .email-footer {
                            padding: 20px;
                        }
                        .email-header h1 {
                            font-size: 24px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-header">
                        <h1>🌟 Tonarinet</h1>
                    </div>
                    
                    <div class="email-body">
                        <h2>%s</h2>
                        <div class="divider"></div>
                        <div class="email-content">
                            %s
                        </div>
                        <div class="divider"></div>
                        <p style="color: #666; font-style: italic;">
                            이 메일은 Tonarinet 서비스에서 자동으로 발송된 메일입니다.
                        </p>
                    </div>
                    
                    <div class="email-footer">
                        <p>&copy; 2025 Tonarinet. All rights reserved.</p>
                        <p>
                            문의사항이 있으시면 
                            <a href="mailto:support@tonarinet.com">support@tonarinet.com</a>으로 
                            연락해주세요.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(subject, subject, body);
    }
    
    // Utility method for sending welcome emails
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Tonarinet에 오신 것을 환영합니다! 🎉";
        String body = """
            <h3>안녕하세요, %s님!</h3>
            <p>Tonarinet에 회원가입해 주셔서 감사합니다.</p>
            <p>이제 다양한 기능들을 사용하실 수 있습니다:</p>
            <ul>
                <li>✨ 커뮤니티 게시판 참여</li>
                <li>💬 실시간 채팅</li>
                <li>📝 개인 프로필 관리</li>
                <li>🔔 알림 설정</li>
            </ul>
            <p>궁금한 점이 있으시면 언제든지 문의해 주세요!</p>
            <br>
            <p><strong>즐거운 Tonarinet 생활 되세요! 🌟</strong></p>
            """.formatted(username);
        sendEmail(to, subject, body);
    }
    
    // Utility method for sending verification emails
    public void sendVerificationEmail(String to, String username, String verificationCode) {
        String subject = "이메일 인증 코드";
        String body = """
            <h3>안녕하세요, %s님!</h3>
            <p>이메일 인증을 위한 코드를 보내드립니다.</p>
            <div style="background-color: #667eea; color: white; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
                <h2 style="margin: 0; letter-spacing: 3px;">%s</h2>
            </div>
            <p><strong>주의사항:</strong></p>
            <ul>
                <li>이 코드는 10분간 유효합니다.</li>
                <li>타인과 공유하지 마세요.</li>
                <li>요청하지 않으셨다면 이 메일을 무시해 주세요.</li>
            </ul>
            """.formatted(username, verificationCode);
        sendEmail(to, subject, body);
    }
    
    // Utility method for sending password reset emails
    public void sendPasswordResetEmail(String to, String username, String resetLink) {
        String subject = "비밀번호 재설정 요청";
        String body = """
            <h3>안녕하세요, %s님!</h3>
            <p>비밀번호 재설정 요청을 받았습니다.</p>
            <p>아래 버튼을 클릭하여 새로운 비밀번호를 설정해 주세요:</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" style="background-color: #667eea; color: white; padding: 15px 30px; 
                   text-decoration: none; border-radius: 5px; display: inline-block; 
                   font-weight: bold;">비밀번호 재설정</a>
            </div>
            <p><strong>보안을 위한 안내:</strong></p>
            <ul>
                <li>이 링크는 24시간 후 만료됩니다.</li>
                <li>요청하지 않으셨다면 이 메일을 무시해 주세요.</li>
                <li>링크를 클릭할 수 없다면 다음 주소를 복사해서 사용하세요:</li>
            </ul>
            <p style="word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 4px;">
                %s
            </p>
            """.formatted(username, resetLink, resetLink);
        sendEmail(to, subject, body);
    }
}
