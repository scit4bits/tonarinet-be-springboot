package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, UserChatRoom.UserChatRoomId> {

    boolean existsByIdUserIdAndIdChatroomId(Integer userId, Integer chatroomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.id.chatroomId = :chatroomId")
    void deleteByChatroomId(@Param("chatroomId") Integer chatroomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.id.chatroomId = :chatroomId AND ucr.id.userId != :userId")
    void deleteByChatroomIdAndUserIdNot(@Param("chatroomId") Integer chatroomId, @Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.id.userId = :userId AND ucr.id.chatroomId = :chatroomId")
    void deleteByUserIdAndChatroomId(@Param("userId") Integer userId, @Param("chatroomId") Integer chatroomId);
}
