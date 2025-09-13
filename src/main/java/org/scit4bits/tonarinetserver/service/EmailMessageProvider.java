package org.scit4bits.tonarinetserver.service;

import org.scit4bits.tonarinetserver.enums.EmailLanguage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailMessageProvider {

    private final Map<EmailLanguage, Map<String, String>> messages = new HashMap<>();

    public EmailMessageProvider() {
        initializeMessages();
    }

    private void initializeMessages() {
        // Korean messages
        Map<String, String> koMessages = new HashMap<>();
        koMessages.put("welcome.subject", "토나리넷에 오신 것을 환영합니다! 🎉");
        koMessages.put("welcome.greeting", "안녕하세요, %s님!");
        koMessages.put("welcome.content", "토나리넷에 회원가입해 주셔서 감사합니다.");
        koMessages.put("welcome.features", "이제 다양한 기능들을 사용하실 수 있습니다:");
        koMessages.put("welcome.feature.community", "✨ 커뮤니티 게시판 참여");
        koMessages.put("welcome.feature.chat", "💬 실시간 채팅");
        koMessages.put("welcome.feature.profile", "📝 개인 프로필 관리");
        koMessages.put("welcome.feature.notifications", "🔔 알림 설정");
        koMessages.put("welcome.contact", "궁금한 점이 있으시면 언제든지 문의해 주세요!");
        koMessages.put("welcome.closing", "즐거운 Tonarinet 생활 되세요! 🌟");

        koMessages.put("verification.subject", "이메일 인증 코드");
        koMessages.put("verification.greeting", "안녕하세요, %s님!");
        koMessages.put("verification.content", "이메일 인증을 위한 코드를 보내드립니다.");
        koMessages.put("verification.warning.title", "주의사항:");
        koMessages.put("verification.warning.expire", "이 코드는 10분간 유효합니다.");
        koMessages.put("verification.warning.private", "타인과 공유하지 마세요.");
        koMessages.put("verification.warning.ignore", "요청하지 않으셨다면 이 메일을 무시해 주세요.");

        koMessages.put("password.reset.subject", "비밀번호 재설정 요청");
        koMessages.put("password.reset.greeting", "안녕하세요, %s님!");
        koMessages.put("password.reset.content", "비밀번호 재설정 요청을 받았습니다.");
        koMessages.put("password.reset.instruction", "아래 버튼을 클릭하여 새로운 비밀번호를 설정해 주세요:");
        koMessages.put("password.reset.button", "비밀번호 재설정");
        koMessages.put("password.reset.security.title", "보안을 위한 안내:");
        koMessages.put("password.reset.security.expire", "이 링크는 24시간 후 만료됩니다.");
        koMessages.put("password.reset.security.ignore", "요청하지 않으셨다면 이 메일을 무시해 주세요.");
        koMessages.put("password.reset.security.copy", "링크를 클릭할 수 없다면 다음 주소를 복사해서 사용하세요:");

        koMessages.put("footer.disclaimer", "이 메일은 Tonarinet 서비스에서 자동으로 발송된 메일입니다.");
        koMessages.put("footer.copyright", "© 2025 Tonarinet. All rights reserved.");
        koMessages.put("footer.support", "문의사항이 있으시면 support@tonarinet.com으로 연락해주세요.");

        messages.put(EmailLanguage.KOR, koMessages);

        // English messages
        Map<String, String> enMessages = new HashMap<>();
        enMessages.put("welcome.subject", "Welcome to Tonarinet! 🎉");
        enMessages.put("welcome.greeting", "Hello, %s!");
        enMessages.put("welcome.content", "Thank you for joining Tonarinet.");
        enMessages.put("welcome.features", "You can now use various features:");
        enMessages.put("welcome.feature.community", "✨ Community Board Participation");
        enMessages.put("welcome.feature.chat", "💬 Real-time Chat");
        enMessages.put("welcome.feature.profile", "📝 Personal Profile Management");
        enMessages.put("welcome.feature.notifications", "🔔 Notification Settings");
        enMessages.put("welcome.contact", "If you have any questions, please feel free to contact us!");
        enMessages.put("welcome.closing", "Enjoy your Tonarinet experience! 🌟");

        enMessages.put("verification.subject", "Email Verification Code");
        enMessages.put("verification.greeting", "Hello, %s!");
        enMessages.put("verification.content", "Here is your email verification code.");
        enMessages.put("verification.warning.title", "Important Notes:");
        enMessages.put("verification.warning.expire", "This code is valid for 10 minutes.");
        enMessages.put("verification.warning.private", "Do not share with others.");
        enMessages.put("verification.warning.ignore", "If you didn't request this, please ignore this email.");

        enMessages.put("password.reset.subject", "Password Reset Request");
        enMessages.put("password.reset.greeting", "Hello, %s!");
        enMessages.put("password.reset.content", "We received a password reset request.");
        enMessages.put("password.reset.instruction", "Click the button below to set a new password:");
        enMessages.put("password.reset.button", "Reset Password");
        enMessages.put("password.reset.security.title", "Security Notice:");
        enMessages.put("password.reset.security.expire", "This link will expire in 24 hours.");
        enMessages.put("password.reset.security.ignore", "If you didn't request this, please ignore this email.");
        enMessages.put("password.reset.security.copy",
                "If you can't click the link, copy and use the following address:");

        enMessages.put("footer.disclaimer", "This email was automatically sent from Tonarinet service.");
        enMessages.put("footer.copyright", "© 2025 Tonarinet. All rights reserved.");
        enMessages.put("footer.support", "For inquiries, please contact us at support@tonarinet.com.");

        messages.put(EmailLanguage.ENG, enMessages);

        // Japanese messages
        Map<String, String> jaMessages = new HashMap<>();
        jaMessages.put("welcome.subject", "となりネットへようこそ！🎉");
        jaMessages.put("welcome.greeting", "こんにちは、%sさん！");
        jaMessages.put("welcome.content", "となりネットにご登録いただき、ありがとうございます。");
        jaMessages.put("welcome.features", "様々な機能をご利用いただけます：");
        jaMessages.put("welcome.feature.community", "✨ コミュニティ掲示板への参加");
        jaMessages.put("welcome.feature.chat", "💬 リアルタイムチャット");
        jaMessages.put("welcome.feature.profile", "📝 個人プロフィール管理");
        jaMessages.put("welcome.feature.notifications", "🔔 通知設定");
        jaMessages.put("welcome.contact", "ご質問がございましたら、お気軽にお問い合わせください！");
        jaMessages.put("welcome.closing", "となりネットライフをお楽しみください！🌟");

        jaMessages.put("verification.subject", "メール認証コード");
        jaMessages.put("verification.greeting", "こんにちは、%sさん！");
        jaMessages.put("verification.content", "メール認証コードをお送りします。");
        jaMessages.put("verification.warning.title", "注意事項：");
        jaMessages.put("verification.warning.expire", "このコードは10分間有効です。");
        jaMessages.put("verification.warning.private", "他人と共有しないでください。");
        jaMessages.put("verification.warning.ignore", "このメールをリクエストしていない場合は、無視してください。");

        jaMessages.put("password.reset.subject", "パスワード再設定のリクエスト");
        jaMessages.put("password.reset.greeting", "こんにちは、%sさん！");
        jaMessages.put("password.reset.content", "パスワード再設定のリクエストを受け付けました。");
        jaMessages.put("password.reset.instruction", "下のボタンをクリックして新しいパスワードを設定してください：");
        jaMessages.put("password.reset.button", "パスワード再設定");
        jaMessages.put("password.reset.security.title", "セキュリティについて：");
        jaMessages.put("password.reset.security.expire", "このリンクは24時間後に期限切れとなります。");
        jaMessages.put("password.reset.security.ignore", "このメールをリクエストしていない場合は、無視してください。");
        jaMessages.put("password.reset.security.copy", "リンクをクリックできない場合は、以下のアドレスをコピーしてご利用ください：");

        jaMessages.put("footer.disclaimer", "このメールはとなりネットサービスから自動送信されました。");
        jaMessages.put("footer.copyright", "© 2025 となりネット All rights reserved.");
        jaMessages.put("footer.support", "お問い合わせは support@tonarinet.com までご連絡ください。");

        messages.put(EmailLanguage.JPN, jaMessages);
    }

    public String getMessage(EmailLanguage language, String key, Object... args) {
        Map<String, String> langMessages = messages.get(language);
        if (langMessages == null) {
            langMessages = messages.get(EmailLanguage.ENG); // Fallback to Korean
        }

        String message = langMessages.get(key);
        if (message == null) {
            return "[Message not found: " + key + "]";
        }

        if (args.length > 0) {
            return String.format(message, args);
        }

        return message;
    }
}