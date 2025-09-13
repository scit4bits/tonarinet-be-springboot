package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatmessage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "sender", nullable = false)
    private Integer senderId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "chatroom_id", nullable = false)
    private Integer chatroomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender", insertable = false, updatable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", insertable = false, updatable = false)
    private ChatRoom chatRoom;
}
