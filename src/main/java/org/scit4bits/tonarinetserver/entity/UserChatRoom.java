package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "userchatroom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChatRoom {

    @EmbeddedId
    private UserChatRoomId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatroomId")
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatroom;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserChatRoomId {

        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "chatroom_id")
        private Integer chatroomId;
    }
}
