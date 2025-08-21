package org.scit4bits.tonarinetserver.service;

import org.scit4bits.tonarinetserver.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret_key}")
    private String jwtSecretKey;
    private Algorithm algorithm;
    private JWTVerifier verifier;
    
    @PostConstruct
    private void init(){
         this.algorithm = Algorithm.HMAC256(jwtSecretKey);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(User user) {
        String jwtToken = JWT.create()
            .withSubject(user.getId().toString())
            .withClaim("userId", user.getId().toString())
            .withClaim("email", user.getEmail())
            .withIssuedAt(new java.util.Date())
            .withExpiresAt(new java.util.Date(System.currentTimeMillis() + 86400000))
            .sign(algorithm);
        
        log.debug("Generated JWT token for user: {}", user.getEmail());
        return jwtToken;
    }
    
    public String extractUserId(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            log.error("JWT verification failed: {}", e.getMessage());
            return null;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public DecodedJWT decodeToken(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("JWT decode failed: {}", e.getMessage());
            return null;
        }
    }
}
