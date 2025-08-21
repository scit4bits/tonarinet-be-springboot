package org.scit4bits.tonarinetserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtService {
    
    private static final String SECRET_KEY = "JWTSecretKeyLOL";
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private final JWTVerifier verifier = JWT.require(algorithm).build();
    
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
