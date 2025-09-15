package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Notification;

import java.time.LocalDateTime;

/**
 * 알림 정보를 전달하기 위한 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Integer id;
    private Integer userId;
    private String contents;
    private String link;
    private LocalDateTime createdAt;
    private Boolean isRead;

    /**
     * Notification 엔티티를 NotificationDTO로 변환합니다.
     * @param notification 변환할 Notification 엔티티
     * @return 변환된 NotificationDTO 객체
     */
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .contents(notification.getContents())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .build();
    }
}
