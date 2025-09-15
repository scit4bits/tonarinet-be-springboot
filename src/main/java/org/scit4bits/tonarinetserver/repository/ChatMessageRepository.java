package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채팅 메시지(ChatMessage) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    /**
     * 특정 채팅방의 모든 메시지를 생성 시간 오름차순으로 조회합니다.
     * @param chatroomId 채팅방 ID
     * @return 메시지 리스트
     */
    List<ChatMessage> findByChatroomIdOrderByCreatedAtAsc(Integer chatroomId);

    /**
     * 특정 채팅방의 메시지를 페이징하여 최신순으로 조회합니다.
     * @param chatroomId 채팅방 ID
     * @param pageable 페이징 정보
     * @return 페이징된 메시지
     */
    Page<ChatMessage> findByChatroomIdOrderByCreatedAtDesc(Integer chatroomId, Pageable pageable);

    /**
     * 특정 채팅방의 읽지 않은 메시지를 조회합니다.
     * (사용자별 읽음 상태를 추적하려면 추가 구현이 필요합니다.)
     * @param chatroomId 채팅방 ID
     * @return 읽지 않은 메시지 리스트
     */
    List<ChatMessage> findByChatroomIdAndIsReadFalse(Integer chatroomId);

    /**
     * 특정 채팅방의 메시지 수를 계산합니다.
     * @param chatroomId 채팅방 ID
     * @return 메시지 수
     */
    Long countByChatroomId(Integer chatroomId);

    /**
     * 특정 발신자를 제외하고 특정 채팅방의 읽지 않은 메시지 수를 계산합니다.
     * @param chatroomId 채팅방 ID
     * @param senderId 제외할 발신자 ID
     * @return 읽지 않은 메시지 수
     */
    Long countByChatroomIdAndIsReadFalseAndSenderIdNot(Integer chatroomId, Integer senderId);

    /**
     * 특정 채팅방의 최근 메시지를 조회합니다. (초기 로딩에 유용)
     * @param chatroomId 채팅방 ID
     * @param pageable 페이징 정보 (조회할 메시지 수 제한)
     * @return 최근 메시지 리스트
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatroomId = :chatroomId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessagesByChatroomId(@Param("chatroomId") Integer chatroomId, Pageable pageable);
}
