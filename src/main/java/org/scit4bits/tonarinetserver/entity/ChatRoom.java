package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 엔티티
 */
@Entity
@Table(name = "chatroom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    /** 채팅방 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 채팅방 제목 */
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    /** 강제 잔류 여부 */
    @Builder.Default
    @Column(name = "force_remain", nullable = false)
    private Boolean forceRemain = false;

    /** 채팅방 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 방장 사용자 ID */
    @Column(name = "leader_user_id", nullable = false)
    private Integer leaderUserId;

    /** 방장 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", insertable = false, updatable = false)
    private User leaderUser;

    /** 채팅방 참여 사용자 목록 */
    @ManyToMany
    @JoinTable(
            name = "userchatroom",
            joinColumns = @JoinColumn(name = "chatroom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    /** 메시지 목록 */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages;
}
