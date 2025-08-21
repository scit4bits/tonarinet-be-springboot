package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String phone;
    private String description;
    private LocalDateTime createdAt;
}