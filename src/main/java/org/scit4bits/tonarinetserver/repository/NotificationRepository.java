package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByUserId(Integer userId);

    List<Notification> findAllByIsReadFalseAndUserId(Integer userId);
}
