package org.scit4bits.tonarinetserver.service;

import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUserByAccessToken(String accessToken){
        try {
            DecodedJWT decodedJWT = JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256("JWTSecretKeyLOL"))
                .build()
                .verify(accessToken);
            
            Integer userId = Integer.parseInt(decodedJWT.getSubject());
            User user = userRepository.findById(userId).get();
            
            if(user != null) {
                log.debug("User found by access token: {}", user.getEmail());
                return user;
            } else {
                log.warn("User not found for userId: {}", userId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error validating access token: {}", e.getMessage());
            return null;
        }
    }

    
}
