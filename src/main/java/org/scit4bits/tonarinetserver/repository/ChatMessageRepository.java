package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    /**
     * Find all messages in a chat room ordered by creation time
     */
    List<ChatMessage> findByChatroomIdOrderByCreatedAtAsc(Integer chatroomId);

    /**
     * Find messages in a chat room with pagination
     */
    Page<ChatMessage> findByChatroomIdOrderByCreatedAtDesc(Integer chatroomId, Pageable pageable);

    /**
     * Find unread messages for a specific user (this would need additional
     * implementation
     * if you want to track read status per user)
     */
    List<ChatMessage> findByChatroomIdAndIsReadFalse(Integer chatroomId);

    /**
     * Count messages in a chat room
     */
    Long countByChatroomId(Integer chatroomId);

    /**
     * Count unread messages in a chat room excluding messages from a specific sender
     */
    Long countByChatroomIdAndIsReadFalseAndSenderIdNot(Integer chatroomId, Integer senderId);

    /**
     * Find recent messages in a chat room (useful for initial load)
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatroomId = :chatroomId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessagesByChatroomId(@Param("chatroomId") Integer chatroomId, Pageable pageable);
}
