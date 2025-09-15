package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채팅방(ChatRoom) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    /**
     * 모든 필드(제목, 설명, 방장 이름)에서 검색어와 일치하는 채팅방을 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 채팅방
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
            "WHERE (:search = '' OR " +
            "LOWER(cr.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(cr.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "(lu.name IS NOT NULL AND LOWER(lu.name) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<ChatRoom> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    /**
     * 제목에 특정 문자열을 포함하는 채팅방을 페이징하여 조회합니다.
     * @param title 검색할 제목 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 채팅방
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
            "WHERE LOWER(cr.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<ChatRoom> findByTitleContaining(@Param("title") String title, Pageable pageable);

    /**
     * 설명에 특정 문자열을 포함하는 채팅방을 페이징하여 조회합니다.
     * @param description 검색할 설명 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 채팅방
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
            "WHERE LOWER(cr.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<ChatRoom> findByDescriptionContaining(@Param("description") String description, Pageable pageable);

    /**
     * 방장 ID로 채팅방을 페이징하여 조회합니다.
     * @param leaderUserId 방장 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 채팅방
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
            "WHERE cr.leaderUserId = :leaderUserId")
    Page<ChatRoom> findByLeaderUserId(@Param("leaderUserId") Integer leaderUserId, Pageable pageable);

    /**
     * 강제 잔류 여부로 채팅방을 페이징하여 조회합니다.
     * @param forceRemain 강제 잔류 여부
     * @param pageable 페이징 정보
     * @return 페이징된 채팅방
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
            "WHERE cr.forceRemain = :forceRemain")
    Page<ChatRoom> findByForceRemain(@Param("forceRemain") Boolean forceRemain, Pageable pageable);

    /**
     * 방장 이름에 특정 문자열을 포함하는 채팅방을 페이징하여 조회합니다.
     * @param username 검색할 방장 이름 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 채팅방
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
            "WHERE lu.name IS NOT NULL AND LOWER(lu.name) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<ChatRoom> findByLeaderUsernameContaining(@Param("username") String username, Pageable pageable);

    /**
     * 특정 사용자가 참여하고 있는 모든 채팅방을 조회합니다.
     * @param userId 사용자 ID
     * @return 채팅방 리스트
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN cr.users u " +
            "WHERE u.id = :userId")
    List<ChatRoom> findByUserId(@Param("userId") Integer userId);

    /**
     * 특정 사용자가 방장인 모든 채팅방을 조회합니다.
     * @param userId 사용자 ID
     * @return 채팅방 리스트
     */
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
            "WHERE cr.leaderUserId = :userId")
    List<ChatRoom> findByLeaderUserId(@Param("userId") Integer userId);
}
