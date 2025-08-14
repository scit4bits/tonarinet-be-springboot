package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    
    private Integer id;
    private String email;
    private String password;
    private String name;
    private LocalDate birth;
    private String nickname;
    private String phone;
    private String description;
    private LocalDateTime createdAt;
    
    // Converter methods
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .birth(user.getBirth())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .description(user.getDescription())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    public User toEntity() {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .name(this.name)
                .birth(this.birth)
                .nickname(this.nickname)
                .phone(this.phone)
                .description(this.description)
                .createdAt(this.createdAt)
                .build();
    }
}