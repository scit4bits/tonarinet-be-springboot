package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 제출물(Submission) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

    /**
     * 모든 필드(내용, 작성자 이름, 과제 이름)에서 검색어와 일치하는 제출물을 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 제출물
     */
    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
            "WHERE (:search = '' OR " +
            "LOWER(s.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.createdBy.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.task.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Submission> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    /**
     * 내용에 특정 문자열을 포함하는 제출물을 페이징하여 조회합니다.
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 제출물
     */
    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
            "WHERE LOWER(s.contents) LIKE LOWER(CONCAT('%', :contents, '%'))")
    Page<Submission> findByContentsContaining(@Param("contents") String contents, Pageable pageable);

    /**
     * 작성자 ID로 제출물을 페이징하여 조회합니다.
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 제출물
     */
    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
            "WHERE s.createdById = :createdById")
    Page<Submission> findByCreatedById(@Param("createdById") Integer createdById, Pageable pageable);

    /**
     * 과제 ID로 제출물을 페이징하여 조회합니다.
     * @param taskId 과제 ID
     * @param pageable 페이징 정보
     * @return 페이징된 제출물
     */
    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
            "WHERE s.taskId = :taskId")
    Page<Submission> findByTaskId(@Param("taskId") Integer taskId, Pageable pageable);

    /**
     * 작성자 닉네임에 특정 문자열을 포함하는 제출물을 페이징하여 조회합니다.
     * @param name 검색할 닉네임 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 제출물
     */
    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
            "WHERE LOWER(s.createdBy.nickname) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Submission> findByCreatedByNicknameContaining(@Param("name") String name, Pageable pageable);

    /**
     * 과제 이름에 특정 문자열을 포함하는 제출물을 페이징하여 조회합니다.
     * @param taskName 검색할 과제 이름 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 제출물
     */
    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
            "WHERE LOWER(s.task.name) LIKE LOWER(CONCAT('%', :taskName, '%'))")
    Page<Submission> findByTaskNameContaining(@Param("taskName") String taskName, Pageable pageable);

    /**
     * 작성자 ID로 모든 제출물을 조회합니다.
     * @param createdById 작성자 ID
     * @return 제출물 리스트
     */
    List<Submission> findByCreatedById(Integer createdById);

    /**
     * 과제 ID로 모든 제출물을 조회합니다.
     * @param taskId 과제 ID
     * @return 제출물 리스트
     */
    List<Submission> findByTaskId(Integer taskId);
}
