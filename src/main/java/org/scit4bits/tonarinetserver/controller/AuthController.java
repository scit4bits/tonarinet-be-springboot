package org.scit4bits.tonarinetserver.controller;

import java.util.LinkedHashMap;

import org.apache.tomcat.util.json.JSONParser;
import org.scit4bits.tonarinetserver.dto.AuthCheckResponse;
import org.scit4bits.tonarinetserver.dto.GenerateStateResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.service.AuthService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // @PostMapping("/create")
    // public ResponseEntity<SimpleResponse> createUser(@RequestBody UserDTO userJson) {
    //     log.debug("userJson: {}", userJson);
    //     userRepository.save(userJson.toEntity());
    //     return ResponseEntity.ok(new SimpleResponse("User created successfully"));
    // }
    
    @GetMapping("/generateState")
    public ResponseEntity<GenerateStateResponse> getGenerateState() {
        GenerateStateResponse response = authService.generateState();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/line/check")
    public ResponseEntity<AuthCheckResponse> getLineCheck(@RequestParam("code") String code, @RequestParam("state") String state) {
        AuthCheckResponse response = authService.getLineCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/google/check")
    public ResponseEntity<AuthCheckResponse> getGoogleCheck(@RequestParam("code") String code, @RequestParam("state") String state) {
        AuthCheckResponse response = authService.getGoogleCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/kakao/check")
    public ResponseEntity<AuthCheckResponse> getKakaoCheck(@RequestParam("code") String code, @RequestParam("state") String state) {
        AuthCheckResponse response = authService.getKakaoCheck(code, state);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    
}
