package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 알림(Notification) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    /**
     * 특정 사용자의 모든 알림을 생성 시간 내림차순으로 조회합니다.
     * @param userId 사용자 ID
     * @return 알림 리스트
     */
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Integer userId);

    /**
     * 특정 사용자의 읽지 않은 모든 알림을 조회합니다.
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 리스트
     */
    List<Notification> findAllByIsReadFalseAndUserId(Integer userId);

    /**
     * 특정 사용자의 읽지 않은 알림 수를 계산합니다.
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 수
     */
    int countByIsReadFalseAndUserId(Integer userId);
}
