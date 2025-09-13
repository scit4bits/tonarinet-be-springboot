package org.scit4bits.tonarinetserver.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.tomcat.util.json.JSONParser;
import org.scit4bits.tonarinetserver.dto.AuthCheckResponse;
import org.scit4bits.tonarinetserver.dto.ChatRoomRequestDTO;
import org.scit4bits.tonarinetserver.dto.GenerateStateResponse;
import org.scit4bits.tonarinetserver.dto.SignUpRequest;
import org.scit4bits.tonarinetserver.entity.Country;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserRole;
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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public GenerateStateResponse generateState() {
        String state = java.util.UUID.randomUUID().toString();
        String nonce = java.util.UUID.randomUUID().toString();
        log.debug("Generated state: {}, nonce: {}", state, nonce);

        return new GenerateStateResponse(state, nonce);
    }

    public AuthCheckResponse getLineCheck(String code, String state) {
        WebClient webClient = WebClient.create();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", lineApiRedirectUri);
        formData.add("client_id", lineApiClientId);
        formData.add("client_secret", lineApiClientSecret);

        try {
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

            // Decode the JWT token
            DecodedJWT decodedJWT = JWT.decode(idToken);
            log.debug("Decoded JWT - Subject: {}, Issuer: {}, Audience: {}",
                    decodedJWT.getSubject(), decodedJWT.getIssuer(), decodedJWT.getAudience());

            // Extract claims
            String userId = decodedJWT.getSubject();
            String name = decodedJWT.getClaim("name").asString();
            String picture = decodedJWT.getClaim("picture").asString();

            log.debug("User ID: {}, Name: {}, Picture: {}", userId, name, picture);

            return new AuthCheckResponse(userId, name, picture);
        } catch (Exception e) {
            log.error("Error parsing JSON response: {}", e.getMessage());
            return null;
        }
    }

    public AuthCheckResponse getGoogleCheck(String code, String state) {
        WebClient webClient = WebClient.create();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleApiClientId);
        formData.add("client_secret", googleApiClientSecret);
        formData.add("redirect_uri", googleApiRedirectUri);
        formData.add("grant_type", "authorization_code");

        try {
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

            // Decode the JWT token
            DecodedJWT decodedJWT = JWT.decode(idToken);
            log.debug("Decoded JWT - Subject: {}, Issuer: {}, Audience: {}",
                    decodedJWT.getSubject(), decodedJWT.getIssuer(), decodedJWT.getAudience());

            // Extract claims
            String userId = decodedJWT.getSubject();
            String name = decodedJWT.getClaim("name").asString();
            String email = decodedJWT.getClaim("email").asString();
            String picture = decodedJWT.getClaim("picture").asString();

            log.debug("User ID: {}, Name: {}, Picture: {}", userId, name, picture);
            log.debug("Email: {}", email);
            return new AuthCheckResponse(userId, name, picture, email);
        } catch (Exception e) {
            log.error("Error parsing JSON response: {}", e.getMessage());
            return null;
        }
    }

    public AuthCheckResponse getKakaoCheck(String code, String state) {
        WebClient webClient = WebClient.create();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", kakaoClientId);
        formData.add("client_secret", kakaoClientSecret);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("grant_type", "authorization_code");

        try {
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

            // Decode the JWT token
            DecodedJWT decodedJWT = JWT.decode(idToken);
            log.debug("Decoded JWT - Subject: {}, Issuer: {}, Audience: {}",
                    decodedJWT.getSubject(), decodedJWT.getIssuer(), decodedJWT.getAudience());

            // Extract claims
            String userId = decodedJWT.getSubject();
            String name = decodedJWT.getClaim("nickname").asString();
            String picture = decodedJWT.getClaim("picture").asString();

            log.debug("User ID: {}, Name: {}, Picture: {}", userId, name, picture);

            return new AuthCheckResponse(userId, name, picture);
        } catch (Exception e) {
            log.error("Error parsing JSON response: {}", e.getMessage());
            return null;
        }
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

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

            // Country set-up is REQUIRED

            // Set up Country relationship (simple many-to-many)
            Country countryEntity = countryRepository.findById(userJson.getNationality()).get();
            List<Country> countryList = new ArrayList<>();
            countryList.add(countryEntity);
            user.setCountries(countryList);
            user.setNationality(countryEntity);

            userRepository.save(user);

            // 1. create new chatroom (chat room name is "AI Chatbot" and chatroom leader is
            // user 0)
            // 2. let user 0 join the chatroom
            // 3. let new user join the chatroom
            try {
                // Find or create system user for AI Chatbot (try ID 0 first, then find any
                // admin user)
                User systemUser = userRepository.findById(0).get();

                // Create ChatRoomRequestDTO for AI Chatbot room
                ChatRoomRequestDTO chatRoomRequest = ChatRoomRequestDTO.builder()
                        .title("AI Chatbot")
                        .description("Welcome to your personal AI assistant chatroom!")
                        .forceRemain(true)
                        .userIds(List.of(user.getId()))
                        .build();

                // Create the chatroom with system user as leader
                chatRoomService.createChatRoom(chatRoomRequest, systemUser);
                log.debug("Created AI Chatbot room for user: {} with leader: {}", user.getId(), systemUser.getId());
            } catch (Exception e) {
                log.warn("Failed to create AI Chatbot room for user {}: {}", user.getId(), e.getMessage());
                // Don't fail the signup process if chatroom creation fails
            }

            // Send mail
            emailService.sendWelcomeEmail(userJson.getNationality(), user.getEmail(), user.getName());

            return true;
        } catch (Exception e) {
            log.error("Error signing up user: {}", e.getMessage());
            return false;
        }
    }

    public String signInWithPassword(String email, String password) {
        User user = userRepository.findByEmail(email).get();
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            log.debug("User authenticated successfully: {}", user.getEmail());
            return jwtService.generateToken(user);
        } else {
            log.warn("Authentication failed for user: {}", email);
            return null;
        }
    }

    public String signInWithOAuth(String provider, String oauthid) { // OAuthID from IDToken (sub)
        User user = userRepository.findByOauthidAndProvider(oauthid, provider).get();
        if (user != null) {
            log.debug("User authenticated successfully with OAuth: {}", user.getEmail());
            return jwtService.generateToken(user);
        } else {
            log.warn("OAuth authentication failed for provider: {} and oauthid: {}", provider, oauthid);
            return null;
        }
    }

    public void sendForgotPasswordEmail(String email) {
        User user = userRepository.findByEmail(email).get();
        String token = jwtService.generateFindPasswordToken(email);
        user.setResetToken(token);
        userRepository.save(user);

        String resetLink = "https://tn.thxx.xyz/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getNationality().getCountryCode(), user.getEmail(), user.getName(),
                resetLink);
        log.info("Sent password reset email to: {}", email);
    }

    public boolean resetPassword(String token, String newPassword) {
        DecodedJWT decodedJWT = jwtService.decodeToken(token);
        if (decodedJWT == null) {
            log.warn("Invalid or expired password reset token");
            return false;
        }

        String email = decodedJWT.getClaim("email").asString();
        User user = userRepository.findByEmail(email).get();
        if (user == null || !token.equals(user.getResetToken())) {
            log.warn("Password reset token does not match for email: {}", email);
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Invalidate the token after use
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", email);
        return true;
    }

    public boolean validateResetToken(String token) {
        DecodedJWT decodedJWT = jwtService.decodeToken(token);
        if (decodedJWT == null) {
            log.warn("Invalid or expired password reset token");
            return false;
        }

        log.debug("Password reset token is valid");
        return true;
    }

}
