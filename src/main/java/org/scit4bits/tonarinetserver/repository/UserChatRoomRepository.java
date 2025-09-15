package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자-채팅방(UserChatRoom) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, UserChatRoom.UserChatRoomId> {

    /**
     * 특정 사용자가 특정 채팅방에 참여하고 있는지 확인합니다.
     * @param userId 사용자 ID
     * @param chatroomId 채팅방 ID
     * @return 참여하고 있으면 true, 아니면 false
     */
    boolean existsByIdUserIdAndIdChatroomId(Integer userId, Integer chatroomId);

    /**
     * 특정 채팅방의 모든 사용자 참여 정보를 삭제합니다.
     * @param chatroomId 채팅방 ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.id.chatroomId = :chatroomId")
    void deleteByChatroomId(@Param("chatroomId") Integer chatroomId);

    /**
     * 특정 사용자를 제외하고 특정 채팅방의 모든 사용자 참여 정보를 삭제합니다.
     * @param chatroomId 채팅방 ID
     * @param userId 제외할 사용자 ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.id.chatroomId = :chatroomId AND ucr.id.userId != :userId")
    void deleteByChatroomIdAndUserIdNot(@Param("chatroomId") Integer chatroomId, @Param("userId") Integer userId);

    /**
     * 특정 사용자의 특정 채팅방 참여 정보를 삭제합니다.
     * @param userId 사용자 ID
     * @param chatroomId 채팅방 ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.id.userId = :userId AND ucr.id.chatroomId = :chatroomId")
    void deleteByUserIdAndChatroomId(@Param("userId") Integer userId, @Param("chatroomId") Integer chatroomId);
}
