package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * OAuth 인증 확인 응답을 위한 DTO
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AuthCheckResponse {
    private final String oauthid;
    private final String name;
    private final String profileImageUrl;
    private String email = null;
}
