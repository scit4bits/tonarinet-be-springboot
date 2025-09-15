package org.scit4bits.tonarinetserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JSON Web Token (JWT) 생성, 검증, 디코딩을 처리하는 서비스입니다.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret_key}")
    private String jwtSecretKey;
    private Algorithm algorithm;
    private JWTVerifier verifier;

    /**
     * 서비스 초기화 시 JWT 알고리즘과 검증기를 설정합니다.
     */
    @PostConstruct
    private void init() {
        this.algorithm = Algorithm.HMAC256(jwtSecretKey);
        this.verifier = JWT.require(algorithm).build();
    }

    /**
     * 사용자를 위한 JWT 토큰을 생성합니다.
     * @param user 토큰을 생성할 사용자 정보
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(User user) {
        // 24시간 유효한 토큰 생성
        String jwtToken = JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("userId", user.getId().toString())
                .withClaim("email", user.getEmail())
                .withIssuedAt(new java.util.Date())
                .withExpiresAt(new java.util.Date(System.currentTimeMillis() + 86400000)) // 24시간
                .sign(algorithm);

        log.debug("사용자 {}에 대한 JWT 토큰 생성 완료", user.getEmail());
        return jwtToken;
    }

    /**
     * 비밀번호 재설정을 위한 JWT 토큰을 생성합니다.
     * @param email 비밀번호를 재설정할 이메일 주소
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateFindPasswordToken(String email) {
        // 5분 유효한 토큰 생성
        String jwtToken = JWT.create()
                .withSubject(email)
                .withClaim("email", email)
                .withIssuedAt(new java.util.Date())
                .withExpiresAt(new java.util.Date(System.currentTimeMillis() + 300000)) // 5분
                .sign(algorithm);

        log.debug("비밀번호 재설정을 위한 JWT 토큰 생성 완료: {}", email);
        return jwtToken;
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String extractUserId(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 토큰의 유효성을 검증합니다.
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("JWT 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰을 디코딩하여 DecodedJWT 객체를 반환합니다.
     * @param token JWT 토큰
     * @return DecodedJWT 객체
     */
    public DecodedJWT decodeToken(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("JWT 디코딩 실패: {}", e.getMessage());
            return null;
        }
    }
}
