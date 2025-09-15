package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.TaskGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 과제 그룹(TaskGroup) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Integer> {

    /**
     * 특정 조직 ID에 속한 모든 과제 그룹을 페이징하여 조회합니다.
     * @param orgId 조직 ID
     * @param pageable 페이징 정보
     * @return 페이징된 과제 그룹
     */
    Page<TaskGroup> findByOrgId(Integer orgId, Pageable pageable);

    /**
     * 특정 조직 ID와 과제 그룹 ID로 과제 그룹을 페이징하여 조회합니다.
     * @param orgId 조직 ID
     * @param id 과제 그룹 ID
     * @param pageable 페이징 정보
     * @return 페이징된 과제 그룹
     */
    Page<TaskGroup> findByOrgIdAndId(Integer orgId, Integer id, Pageable pageable);

    /**
     * 특정 조직 ID 내에서 제목에 특정 문자열을 포함하는 과제 그룹을 페이징하여 조회합니다. (대소문자 무시)
     * @param orgId 조직 ID
     * @param title 검색할 제목 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 과제 그룹
     */
    Page<TaskGroup> findByOrgIdAndTitleContainingIgnoreCase(Integer orgId, String title, Pageable pageable);

    /**
     * 특정 조직 ID 내에서 내용에 특정 문자열을 포함하는 과제 그룹을 페이징하여 조회합니다. (대소문자 무시)
     * @param orgId 조직 ID
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 과제 그룹
     */
    Page<TaskGroup> findByOrgIdAndContentsContainingIgnoreCase(Integer orgId, String contents, Pageable pageable);

    /**
     * 특정 조직 ID 내에서 모든 필드(제목, 내용)에서 검색어와 일치하는 과제 그룹을 페이징하여 조회합니다.
     * @param orgId 조직 ID
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 과제 그룹
     */
    @Query("SELECT tg FROM TaskGroup tg WHERE tg.orgId = :orgId AND (" +
            "LOWER(tg.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(tg.contents) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TaskGroup> findByOrgIdAndAllFieldsContaining(@Param("orgId") Integer orgId, @Param("search") String search, Pageable pageable);
}
