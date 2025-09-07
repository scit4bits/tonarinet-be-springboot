package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.TaskGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Integer>{
    
    // 조직 ID로 검색 - 모든 검색은 조직 범위 내에서만 수행됨
    Page<TaskGroup> findByOrgId(Integer orgId, Pageable pageable);
    
    // 조직 ID와 ID로 검색
    Page<TaskGroup> findByOrgIdAndId(Integer orgId, Integer id, Pageable pageable);
    
    // 조직 ID와 제목으로 검색
    Page<TaskGroup> findByOrgIdAndTitleContainingIgnoreCase(Integer orgId, String title, Pageable pageable);
    
    // 조직 ID와 내용으로 검색
    Page<TaskGroup> findByOrgIdAndContentsContainingIgnoreCase(Integer orgId, String contents, Pageable pageable);
    
    // 조직 ID와 전체 필드 검색 - 제목 또는 내용에서 검색
    @Query("SELECT tg FROM TaskGroup tg WHERE tg.orgId = :orgId AND (" +
           "LOWER(tg.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(tg.contents) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TaskGroup> findByOrgIdAndAllFieldsContaining(@Param("orgId") Integer orgId, @Param("search") String search, Pageable pageable);
}
