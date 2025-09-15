package org.scit4bits.tonarinetserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.scit4bits.tonarinetserver.dto.AuthCheckResponse;
import org.scit4bits.tonarinetserver.dto.ChatRoomRequestDTO;
import org.scit4bits.tonarinetserver.dto.GenerateStateResponse;
import org.scit4bits.tonarinetserver.dto.SignUpRequest;
import org.scit4bits.tonarinetserver.entity.Country;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.CountryRepository;
import org.scit4bits.tonarinetserver.repository.OrganizationRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 사용자 인증, 회원가입, OAuth 통합 및 비밀번호 관리와 관련된 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final ChatRoomService chatRoomService;

    @Value("${line.api.client_id}")
    private String lineApiClientId;

    @Value("${line.api.client_secret}")
    private String lineApiClientSecret;

    @Value("${line.api.redirect_uri}")
    private String lineApiRedirectUri;

    @Value("${google.api.client_id}")
    private String googleApiClientId;

    @Value("${google.api.client_secret}")
    private String googleApiClientSecret;

    @Value("${google.api.redirect_uri}")
    private String googleApiRedirectUri;

    @Value("${kakao.client_id}")
    private String kakaoClientId;

    @Value("${kakao.client_secret}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect_uri}")
    private String kakaoRedirectUri;

    /**
     * OAuth 인증에 사용할 state와 nonce를 생성합니다.
     * @return 생성된 state와 nonce를 담은 응답 객체
     */
    public GenerateStateResponse generateState() {
        String state = java.util.UUID.randomUUID().toString();
        String nonce = java.util.UUID.randomUUID().toString();
        log.debug("Generated state: {}, nonce: {}", state, nonce);

        return new GenerateStateResponse(state, nonce);
    }

    /**
     * LINE OAuth 인증 코드를 사용하여 사용자 정보를 확인합니다.
     * @param code 인증 코드
     * @param state CSRF 방지를 위한 state 값
     * @return 인증된 사용자 정보를 담은 응답 객체
     */
    public AuthCheckResponse getLineCheck(String code, String state) {
        WebClient webClient = WebClient.create();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", lineApiRedirectUri);
        formData.add("client_id", lineApiClientId);
        formData.add("client_secret", lineApiClientSecret);

        try {
            // LINE 토큰 API 호출
            String response = webClient.post()
                    .uri("https://api.line.me/oauth2/v2.1/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Line token response: {}", response);

            JSONParser parser = new JSONParser(response);
            LinkedHashMap<String, Object> jsonMap = parser.parseObject();
            String idToken = (String) jsonMap.get("id_token");
            log.debug("Extracted id_token: {}", idToken);

            // JWT 토큰 디코딩
            DecodedJWT decodedJWT = JWT.decode(idToken);
            log.debug("Decoded JWT - Subject: {}, Issuer: {}, Audience: {}",
                    decodedJWT.getSubject(), decodedJWT.getIssuer(), decodedJWT.getAudience());

            // 클레임 추출
            String userId = decodedJWT.getSubject();
            String name = decodedJWT.getClaim("name").asString();
            String picture = decodedJWT.getClaim("picture").asString();

            log.debug("User ID: {}, Name: {}, Picture: {}", userId, name, picture);

            return new AuthCheckResponse(userId, name, picture);
        } catch (Exception e) {
            log.error("JSON 응답 파싱 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Google OAuth 인증 코드를 사용하여 사용자 정보를 확인합니다.
     * @param code 인증 코드
     * @param state CSRF 방지를 위한 state 값
     * @return 인증된 사용자 정보를 담은 응답 객체
     */
    public AuthCheckResponse getGoogleCheck(String code, String state) {
        WebClient webClient = WebClient.create();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleApiClientId);
        formData.add("client_secret", googleApiClientSecret);
        formData.add("redirect_uri", googleApiRedirectUri);
        formData.add("grant_type", "authorization_code");

        try {
            // Google 토큰 API 호출
            String response = webClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Google token response: {}", response);

            JSONParser parser = new JSONParser(response);
            LinkedHashMap<String, Object> jsonMap = parser.parseObject();
            String idToken = (String) jsonMap.get("id_token");
            log.debug("Extracted id_token: {}", idToken);

            // JWT 토큰 디코딩
            DecodedJWT decodedJWT = JWT.decode(idToken);
            log.debug("Decoded JWT - Subject: {}, Issuer: {}, Audience: {}",
                    decodedJWT.getSubject(), decodedJWT.getIssuer(), decodedJWT.getAudience());

            // 클레임 추출
            String userId = decodedJWT.getSubject();
            String name = decodedJWT.getClaim("name").asString();
            String email = decodedJWT.getClaim("email").asString();
            String picture = decodedJWT.getClaim("picture").asString();

            log.debug("User ID: {}, Name: {}, Picture: {}", userId, name, picture);
            log.debug("Email: {}", email);
            return new AuthCheckResponse(userId, name, picture, email);
        } catch (Exception e) {
            log.error("JSON 응답 파싱 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Kakao OAuth 인증 코드를 사용하여 사용자 정보를 확인합니다.
     * @param code 인증 코드
     * @param state CSRF 방지를 위한 state 값
     * @return 인증된 사용자 정보를 담은 응답 객체
     */
    public AuthCheckResponse getKakaoCheck(String code, String state) {
        WebClient webClient = WebClient.create();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", kakaoClientId);
        formData.add("client_secret", kakaoClientSecret);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("grant_type", "authorization_code");

        try {
            // Kakao 토큰 API 호출
            String response = webClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Kakao token response: {}", response);

            JSONParser parser = new JSONParser(response);
            LinkedHashMap<String, Object> jsonMap = parser.parseObject();
            String idToken = (String) jsonMap.get("id_token");
            log.debug("Extracted id_token: {}", idToken);

            // JWT 토큰 디코딩
            DecodedJWT decodedJWT = JWT.decode(idToken);
            log.debug("Decoded JWT - Subject: {}, Issuer: {}, Audience: {}",
                    decodedJWT.getSubject(), decodedJWT.getIssuer(), decodedJWT.getAudience());

            // 클레임 추출
            String userId = decodedJWT.getSubject();
            String name = decodedJWT.getClaim("nickname").asString();
            String picture = decodedJWT.getClaim("picture").asString();

            log.debug("User ID: {}, Name: {}, Picture: {}", userId, name, picture);

            return new AuthCheckResponse(userId, name, picture);
        } catch (Exception e) {
            log.error("JSON 응답 파싱 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 이메일 사용 가능 여부를 확인합니다.
     * @param email 확인할 이메일
     * @return 사용 가능 여부
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 사용자 회원가입을 처리합니다.
     * @param userJson 회원가입 요청 정보
     * @return 회원가입 성공 여부
     */
    public boolean userSignUp(SignUpRequest userJson) {
        try {
            log.debug("userJson: {}", userJson);
            User user = User.builder()
                    .email(userJson.getEmail())
                    .password(passwordEncoder.encode(userJson.getPassword()))
                    .name(userJson.getName())
                    .birth(userJson.getBirth())
                    .nickname(userJson.getNickname())
                    .phone(userJson.getPhone())
                    .provider(userJson.getProvider())
                    .oauthid(userJson.getOauthid())
                    .isAdmin(false)
                    .gender(userJson.getGender())
                    .build();

            // 국적 설정은 필수입니다.
            Country countryEntity = countryRepository.findById(userJson.getNationality()).get();
            List<Country> countryList = new ArrayList<>();
            countryList.add(countryEntity);
            user.setCountries(countryList);
            user.setNationality(countryEntity);

            userRepository.save(user);

            // 회원가입 시 AI 챗봇 채팅방을 생성합니다.
            try {
                User systemUser = userRepository.findById(0).get();

                ChatRoomRequestDTO chatRoomRequest = ChatRoomRequestDTO.builder()
                        .title("AI Chatbot")
                        .description("Welcome to your personal AI assistant chatroom!")
                        .forceRemain(true)
                        .userIds(List.of(user.getId()))
                        .build();

                chatRoomService.createChatRoom(chatRoomRequest, systemUser);
                log.debug("사용자 {}를 위해 AI 챗봇 채팅방 생성, 방장: {}", user.getId(), systemUser.getId());
            } catch (Exception e) {
                log.warn("사용자 {}의 AI 챗봇 채팅방 생성 실패: {}", user.getId(), e.getMessage());
            }

            // 환영 이메일 발송
            emailService.sendWelcomeEmail(userJson.getNationality(), user.getEmail(), user.getName());

            return true;
        } catch (Exception e) {
            log.error("사용자 회원가입 오류: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 이메일과 비밀번호로 로그인합니다.
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return JWT 토큰
     */
    public String signInWithPassword(String email, String password) {
        User user = userRepository.findByEmail(email).get();
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            log.debug("사용자 인증 성공: {}", user.getEmail());
            return jwtService.generateToken(user);
        } else {
            log.warn("사용자 인증 실패: {}", email);
            return null;
        }
    }

    /**
     * OAuth로 로그인합니다.
     * @param provider OAuth 제공자 (e.g., "google", "line", "kakao")
     * @param oauthid OAuth 제공자로부터 받은 사용자 ID
     * @return JWT 토큰
     */
    public String signInWithOAuth(String provider, String oauthid) {
        User user = userRepository.findByOauthidAndProvider(oauthid, provider).get();
        if (user != null) {
            log.debug("OAuth를 통한 사용자 인증 성공: {}", user.getEmail());
            return jwtService.generateToken(user);
        } else {
            log.warn("OAuth 인증 실패 - 제공자: {}, OAuth ID: {}", provider, oauthid);
            return null;
        }
    }

    /**
     * 비밀번호 재설정 이메일을 발송합니다.
     * @param email 비밀번호를 재설정할 사용자 이메일
     */
    public void sendForgotPasswordEmail(String email) {
        User user = userRepository.findByEmail(email).get();
        String token = jwtService.generateFindPasswordToken(email);
        user.setResetToken(token);
        userRepository.save(user);

        String resetLink = "https://tn.thxx.xyz/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getNationality().getCountryCode(), user.getEmail(), user.getName(),
                resetLink);
        log.info("비밀번호 재설정 이메일 발송 완료: {}", email);
    }

    /**
     * 비밀번호를 재설정합니다.
     * @param token 비밀번호 재설정 토큰
     * @param newPassword 새로운 비밀번호
     * @return 성공 여부
     */
    public boolean resetPassword(String token, String newPassword) {
        DecodedJWT decodedJWT = jwtService.decodeToken(token);
        if (decodedJWT == null) {
            log.warn("유효하지 않거나 만료된 비밀번호 재설정 토큰입니다.");
            return false;
        }

        String email = decodedJWT.getClaim("email").asString();
        User user = userRepository.findByEmail(email).get();
        if (user == null || !token.equals(user.getResetToken())) {
            log.warn("이메일 {}에 대한 비밀번호 재설정 토큰이 일치하지 않습니다.", email);
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // 사용 후 토큰 무효화
        userRepository.save(user);
        log.info("사용자 {}의 비밀번호 재설정 성공", email);
        return true;
    }

    /**
     * 비밀번호 재설정 토큰의 유효성을 검사합니다.
     * @param token 검사할 토큰
     * @return 유효성 여부
     */
    public boolean validateResetToken(String token) {
        DecodedJWT decodedJWT = jwtService.decodeToken(token);
        if (decodedJWT == null) {
            log.warn("유효하지 않거나 만료된 비밀번호 재설정 토큰입니다.");
            return false;
        }

        log.debug("비밀번호 재설정 토큰이 유효합니다.");
        return true;
    }

}
