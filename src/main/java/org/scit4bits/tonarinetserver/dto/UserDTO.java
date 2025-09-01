package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer id;
    private String email;
    private String name;
    private LocalDate birth;
    private String nickname;
    private String nationality;
    private String phone;
    private String description;
    private Boolean isAdmin;
    private LocalDateTime createdAt;


    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .birth(user.getBirth())
            .nickname(user.getNickname())
            .nationality(user.getNationality() != null ? user.getNationality().getCountryCode() : null)
            .phone(user.getPhone())
            .description(user.getDescription())
            .isAdmin(user.getIsAdmin())
            .createdAt(user.getCreatedAt())
            .build();
    }
}