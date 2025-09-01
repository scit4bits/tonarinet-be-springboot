package org.scit4bits.tonarinetserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.Notification;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal User user) {
        if(user == null) {
            // 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/readall")
    public ResponseEntity<SimpleResponse> readAllNotifications(@AuthenticationPrincipal User user) {
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.readAllNotifications(user.getId());
        return ResponseEntity.ok(new SimpleResponse("All notifications marked as read"));
    }

    @GetMapping("/read")
    public ResponseEntity<SimpleResponse> readOneNotification(@AuthenticationPrincipal User user, @RequestParam("notiId") Integer notiId) {
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.readOneNotification(user.getId(), notiId);
        return ResponseEntity.ok(new SimpleResponse("Notification marked as read"));
    }
    
    
    
}
