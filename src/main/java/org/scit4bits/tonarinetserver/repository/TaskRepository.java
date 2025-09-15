package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 과제(Task) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    /**
     * ID로 과제를 페이징하여 조회합니다.
     * @param id 과제 ID
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    Page<Task> findById(Integer id, Pageable pageable);

    /**
     * 이름에 특정 문자열을 포함하는 과제를 페이징하여 조회합니다. (대소문자 무시)
     * @param name 검색할 이름 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    Page<Task> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 내용에 특정 문자열을 포함하는 과제를 페이징하여 조회합니다. (대소문자 무시)
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    Page<Task> findByContentsContainingIgnoreCase(String contents, Pageable pageable);

    /**
     * 작성자 ID로 과제를 페이징하여 조회합니다.
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    Page<Task> findByCreatedById(Integer createdById, Pageable pageable);

    /**
     * 할당된 사용자 ID로 과제를 페이징하여 조회합니다.
     * @param userId 할당된 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    Page<Task> findByUserId(Integer userId, Pageable pageable);

    /**
     * 할당된 팀 ID로 과제를 페이징하여 조회합니다.
     * @param teamId 할당된 팀 ID
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    Page<Task> findByTeamId(Integer teamId, Pageable pageable);

    /**
     * 과제 그룹 ID로 과제를 페이징하여 조회합니다.
     * @param taskGroupId 과제 그룹 ID
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    Page<Task> findByTaskGroupId(Integer taskGroupId, Pageable pageable);

    /**
     * 특정 사용자에게 할당된 모든 과제를 마감일 오름차순으로 조회합니다.
     * @param userId 사용자 ID
     * @return 과제 리스트
     */
    List<Task> findByUserIdOrderByDueDateAsc(Integer userId);

    /**
     * 특정 팀에 할당된 모든 과제를 마감일 오름차순으로 조회합니다.
     * @param teamId 팀 ID
     * @return 과제 리스트
     */
    List<Task> findByTeamIdOrderByDueDateAsc(Integer teamId);

    /**
     * 모든 필드(이름, 내용)에서 검색어와 일치하는 과제를 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 과제
     */
    @Query("SELECT t FROM Task t WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.contents) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Task> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
