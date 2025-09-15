package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 팀(Team) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    /**
     * ID로 팀을 페이징하여 조회합니다.
     * @param id 팀 ID
     * @param pageable 페이징 정보
     * @return 페이징된 팀
     */
    Page<Team> findById(Integer id, Pageable pageable);

    /**
     * 이름에 특정 문자열을 포함하는 팀을 페이징하여 조회합니다. (대소문자 무시)
     * @param name 검색할 이름 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 팀
     */
    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 리더 사용자 ID로 팀을 페이징하여 조회합니다.
     * @param leaderUserId 리더 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 팀
     */
    Page<Team> findByLeaderUserId(Integer leaderUserId, Pageable pageable);

    /**
     * 조직 ID로 팀을 페이징하여 조회합니다.
     * @param orgId 조직 ID
     * @param pageable 페이징 정보
     * @return 페이징된 팀
     */
    Page<Team> findByOrgId(Integer orgId, Pageable pageable);

    /**
     * 특정 조직의 모든 팀을 이름순으로 조회합니다.
     * @param orgId 조직 ID
     * @return 팀 리스트
     */
    List<Team> findByOrgIdOrderByName(Integer orgId);

    /**
     * 모든 필드(이름)에서 검색어와 일치하는 팀을 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 팀
     */
    @Query("SELECT t FROM Team t WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Team> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
