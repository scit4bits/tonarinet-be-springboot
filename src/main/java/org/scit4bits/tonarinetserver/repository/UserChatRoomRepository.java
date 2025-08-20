package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, UserChatRoom.UserChatRoomId> {

}
