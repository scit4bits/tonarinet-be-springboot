package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
           "WHERE (:search = '' OR " +
           "LOWER(cr.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cr.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "(lu.name IS NOT NULL AND LOWER(lu.name) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<ChatRoom> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
           "WHERE LOWER(cr.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<ChatRoom> findByTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
           "WHERE LOWER(cr.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Page<ChatRoom> findByDescriptionContaining(@Param("description") String description, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
           "WHERE cr.leaderUserId = :leaderUserId")
    Page<ChatRoom> findByLeaderUserId(@Param("leaderUserId") Integer leaderUserId, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
           "WHERE cr.forceRemain = :forceRemain")
    Page<ChatRoom> findByForceRemain(@Param("forceRemain") Boolean forceRemain, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
           "WHERE lu.name IS NOT NULL AND LOWER(lu.name) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<ChatRoom> findByLeaderUsernameContaining(@Param("username") String username, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN cr.users u " +
           "WHERE u.id = :userId")
    List<ChatRoom> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN cr.leaderUser lu LEFT JOIN FETCH cr.users " +
           "WHERE cr.leaderUserId = :userId")
    List<ChatRoom> findByLeaderUserId(@Param("userId") Integer userId);
}
