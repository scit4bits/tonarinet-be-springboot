package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.NotificationDTO;
import org.scit4bits.tonarinetserver.entity.Notification;
import org.scit4bits.tonarinetserver.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Integer userId) {
        /**
         * 두 가지 방법이 존재한다.
         * 1. User를 다시 가져와서 (dbUser) JPA를 이용, Notifcation을 Lazy Load
         * 2. NotificationRepository 에 메소드를 만들어서 직접 가져옴.
         * 이번 경우는 2번 방법을 사용하겠다.
         */
        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }

    public void readAllNotifications(Integer userId) {
        List<Notification> notifications = notificationRepository.findAllByIsReadFalseAndUserId(userId);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void readOneNotification(Integer id, Integer notiId) {
        Notification notification = notificationRepository.findById(notiId).get();
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public int getUnreadCount(Integer userId) {
        return notificationRepository.countByIsReadFalseAndUserId(userId);
    }

    // add new Notification to the user
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
