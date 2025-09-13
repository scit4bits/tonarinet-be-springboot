package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    // ID로 검색
    Page<Task> findById(Integer id, Pageable pageable);

    // 이름으로 검색
    Page<Task> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 내용으로 검색
    Page<Task> findByContentsContainingIgnoreCase(String contents, Pageable pageable);

    // 생성자 ID로 검색
    Page<Task> findByCreatedById(Integer createdById, Pageable pageable);

    // 할당된 사용자 ID로 검색
    Page<Task> findByUserId(Integer userId, Pageable pageable);

    // 할당된 팀 ID로 검색
    Page<Task> findByTeamId(Integer teamId, Pageable pageable);

    // 태스크 그룹 ID로 검색
    Page<Task> findByTaskGroupId(Integer taskGroupId, Pageable pageable);

    // 특정 사용자에게 할당된 태스크들
    List<Task> findByUserIdOrderByDueDateAsc(Integer userId);

    // 특정 팀에 할당된 태스크들
    List<Task> findByTeamIdOrderByDueDateAsc(Integer teamId);

    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT t FROM Task t WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.contents) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Task> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
