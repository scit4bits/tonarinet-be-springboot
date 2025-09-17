package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 정보를 전달하기 위한 DTO
 */
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
    private String entryMessage; // 직접 주입
    private Boolean isGranted; // 직접 주입
    private String role; // 직접 주입
    private Integer profileFileId;
    private LocalDateTime approvedAt; // 직접 주입


    /**
     * User 엔티티를 UserDTO로 변환합니다.
     * @param user 변환할 User 엔티티
     * @return 변환된 UserDTO 객체
     */
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
                .profileFileId(user.getProfileFileId())
                .build();
    }
}
