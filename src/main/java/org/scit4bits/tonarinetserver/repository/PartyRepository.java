package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 파티(Party) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface PartyRepository extends JpaRepository<Party, Integer> {

    /**
     * ID로 파티를 페이징하여 조회합니다.
     * @param id 파티 ID
     * @param pageable 페이징 정보
     * @return 페이징된 파티
     */
    Page<Party> findById(Integer id, Pageable pageable);

    /**
     * 이름에 특정 문자열을 포함하는 파티를 페이징하여 조회합니다. (대소문자 무시)
     * @param name 검색할 파티 이름
     * @param pageable 페이징 정보
     * @return 페이징된 파티
     */
    Page<Party> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 리더 사용자 ID로 파티를 페이징하여 조회합니다.
     * @param leaderUserId 리더 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 파티
     */
    Page<Party> findByLeaderUserId(Integer leaderUserId, Pageable pageable);

    /**
     * 모든 필드(이름)에서 검색어와 일치하는 파티를 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 파티
     */
    @Query("SELECT p FROM Party p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Party> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
