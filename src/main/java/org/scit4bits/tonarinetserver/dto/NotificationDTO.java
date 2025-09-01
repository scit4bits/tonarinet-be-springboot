package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
