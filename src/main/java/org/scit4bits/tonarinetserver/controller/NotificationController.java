package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.NotificationDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 알림 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 현재 로그인한 사용자의 모든 알림을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return NotificationDTO 리스트
     */
    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(@AuthenticationPrincipal User user) {
        if (user == null) {
            // 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<NotificationDTO> notifications = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * 현재 로그인한 사용자의 모든 알림을 읽음으로 표시합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @GetMapping("/readall")
    public ResponseEntity<SimpleResponse> readAllNotifications(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.readAllNotifications(user.getId());
        return ResponseEntity.ok(new SimpleResponse("All notifications marked as read"));
    }

    /**
     * 특정 알림을 읽음으로 표시합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param notiId 읽음으로 표시할 알림 ID
     * @return 성공 응답
     */
    @GetMapping("/read")
    public ResponseEntity<SimpleResponse> readOneNotification(@AuthenticationPrincipal User user, @RequestParam("notiId") Integer notiId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.readOneNotification(user.getId(), notiId);
        return ResponseEntity.ok(new SimpleResponse("Notification marked as read"));
    }

    /**
     * 읽지 않은 알림의 개수를 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return 읽지 않은 알림 개수
     */
    @GetMapping("/unreadCount")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        int unreadCount = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(unreadCount);
    }

}
