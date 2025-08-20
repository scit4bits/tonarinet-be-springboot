package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AuthCheckResponse {
    private final String userId;
    private final String name;
    private final String profileImageUrl;
    private String email = null;
}
