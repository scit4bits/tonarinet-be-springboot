package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자-채팅방 엔티티
 */
@Entity
@Table(name = "userchatroom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChatRoom {

    /** 복합 키 */
    @EmbeddedId
    private UserChatRoomId id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /** 채팅방 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatroomId")
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatroom;

    /**
     * 사용자-채팅방 ID 복합 키 클래스
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserChatRoomId {

        /** 사용자 ID */
        @Column(name = "user_id")
        private Integer userId;

        /** 채팅방 ID */
        @Column(name = "chatroom_id")
        private Integer chatroomId;
    }
}

