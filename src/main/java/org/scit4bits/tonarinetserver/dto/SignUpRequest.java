package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    private String email;
    private String password;
    private String name;
    private LocalDate birth;
    private String nickname;
    private String phone;
    private String provider;
    private String oauthid;
    private String country;
    private String org;
    private String role;
}