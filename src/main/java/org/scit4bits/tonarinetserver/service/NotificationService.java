package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.NotificationDTO;
import org.scit4bits.tonarinetserver.entity.Notification;
import org.scit4bits.tonarinetserver.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 알림 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 특정 사용자의 모든 알림을 조회합니다.
     * @param userId 사용자 ID
     * @return NotificationDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Integer userId) {
        // 알림을 가져오는 방법에는 두 가지가 있습니다.
        // 1. User 엔티티를 다시 조회하여 JPA의 Lazy Loading을 통해 알림을 가져오는 방법
        // 2. NotificationRepository에 직접 메소드를 만들어 조회하는 방법
        // 여기서는 2번 방법을 사용합니다.
        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }

    /**
     * 특정 사용자의 모든 알림을 읽음으로 표시합니다.
     * @param userId 사용자 ID
     */
    public void readAllNotifications(Integer userId) {
        List<Notification> notifications = notificationRepository.findAllByIsReadFalseAndUserId(userId);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * 특정 알림 하나를 읽음으로 표시합니다.
     * @param id 사용자 ID (현재는 사용되지 않음)
     * @param notiId 알림 ID
     */
    public void readOneNotification(Integer id, Integer notiId) {
        Notification notification = notificationRepository.findById(notiId).get();
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * 읽지 않은 알림의 개수를 조회합니다.
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    public int getUnreadCount(Integer userId) {
        return notificationRepository.countByIsReadFalseAndUserId(userId);
    }

    /**
     * 사용자에게 새로운 알림을 추가합니다.
     * @param userId 사용자 ID
     * @param message 알림 내용
     * @param link 알림 클릭 시 이동할 링크
     */
    public void addNotification(Integer userId, String message, String link) {
        Notification notification = Notification.builder()
                .userId(userId)
                .contents(message)
                .link(link)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }


}
