package org.scit4bits.tonarinetserver.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.tomcat.util.json.JSONParser;
import org.scit4bits.tonarinetserver.dto.AuthCheckResponse;
import org.scit4bits.tonarinetserver.dto.GenerateStateResponse;
import org.scit4bits.tonarinetserver.dto.SignInOAuthRequest;
import org.scit4bits.tonarinetserver.dto.SignUpRequest;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.UserDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRoleRepository userRoleRepository;
    
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

    private final BCryptPasswordEncoder passwordEncoder;



    public GenerateStateResponse generateState() {
        String state = java.util.UUID.randomUUID().toString();
        String nonce = java.util.UUID.randomUUID().toString();
        log.debug("Generated state: {}, nonce: {}", state, nonce);

        return new GenerateStateResponse(state,nonce);
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

    public boolean userSignUp(SignUpRequest userJson) {
        try{
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
                    .build();

            // Set up Country relationship (simple many-to-many)
            Country countryEntity = countryRepository.findById(userJson.getCountry()).get();
            List<Country> countryList = new ArrayList<>();
            countryList.add(countryEntity);
            user.setCountries(countryList);

            // Save user first to get the generated ID
            User savedUser = userRepository.save(user);

            // Set up Organization-Role relationship using UserRole entity
            Organization organizationEntity = organizationRepository.findByName(userJson.getOrg()).get();
            
            UserRole userRole = UserRole.builder()
                    .id(UserRole.UserRoleId.builder()
                            .userId(savedUser.getId())
                            .orgId(organizationEntity.getId())
                            .role(userJson.getRole())
                            .build())
                    .user(savedUser)
                    .organization(organizationEntity)
                    .build();
            
            userRoleRepository.save(userRole);
            
            return true;
        }catch(Exception e){
            log.error("Error signing up user: {}", e.getMessage());
            return false;
        }
    }

    public String generateToken(User user){
        String jwtToken = JWT.create()
            .withSubject(user.getId().toString())
            .withClaim("userId", user.getId().toString())
            .withClaim("email", user.getEmail())
            .withIssuedAt(new java.util.Date())
            .withExpiresAt(new java.util.Date(System.currentTimeMillis() + 86400000)) // 24 hours
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("JWTSecretKeyLOL"));
        return jwtToken;
    }

    public String signInWithPassword(String email, String password){
        User user = userRepository.findByEmail(email).get();
        if(user != null && passwordEncoder.matches(password, user.getPassword())){
            log.debug("User authenticated successfully: {}", user.getEmail());
            return generateToken(user);
        } else {
            log.warn("Authentication failed for user: {}", email);
            return null;
        }
    }

    public String signInWithOAuth(String provider, String oauthid){ // OAuthID from IDToken (sub)
        User user = userRepository.findByOauthidAndProvider(oauthid, provider).get();
        if(user != null) {
            log.debug("User authenticated successfully with OAuth: {}", user.getEmail());
            return generateToken(user);
        } else {
            log.warn("OAuth authentication failed for provider: {} and oauthid: {}", provider, oauthid);
            return null;
        }
    }

    

    
}
