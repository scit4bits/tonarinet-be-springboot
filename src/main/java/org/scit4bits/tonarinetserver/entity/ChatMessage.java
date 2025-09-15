package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 */
@Entity
@Table(name = "chatmessage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    /** 메시지 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 발신자 ID */
    @Column(name = "sender", nullable = false)
    private Integer senderId;

    /** 메시지 내용 */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 읽음 여부 */
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /** 채팅방 ID */
    @Column(name = "chatroom_id", nullable = false)
    private Integer chatroomId;

    /** 발신자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender", insertable = false, updatable = false)
    private User sender;

    /** 채팅방 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", insertable = false, updatable = false)
    private ChatRoom chatRoom;
}
