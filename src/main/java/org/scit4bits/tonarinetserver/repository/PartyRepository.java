package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Integer> {
    
    // ID로 검색
    Page<Party> findById(Integer id, Pageable pageable);
    
    // 이름으로 검색
    Page<Party> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // 리더 사용자 ID로 검색
    Page<Party> findByLeaderUserId(Integer leaderUserId, Pageable pageable);
    
    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT p FROM Party p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Party> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
