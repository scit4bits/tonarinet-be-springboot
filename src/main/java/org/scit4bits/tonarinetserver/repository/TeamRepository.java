package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    // ID로 검색
    Page<Team> findById(Integer id, Pageable pageable);

    // 이름으로 검색
    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 리더 사용자 ID로 검색
    Page<Team> findByLeaderUserId(Integer leaderUserId, Pageable pageable);

    // 조직 ID로 검색
    Page<Team> findByOrgId(Integer orgId, Pageable pageable);

    // 특정 조직의 팀들
    List<Team> findByOrgIdOrderByName(Integer orgId);

    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT t FROM Team t WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Team> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
