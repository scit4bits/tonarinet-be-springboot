package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
           "WHERE (:search = '' OR " +
           "LOWER(s.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.createdBy.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.task.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Submission> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
           "WHERE LOWER(s.contents) LIKE LOWER(CONCAT('%', :contents, '%'))")
    Page<Submission> findByContentsContaining(@Param("contents") String contents, Pageable pageable);

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
           "WHERE s.createdById = :createdById")
    Page<Submission> findByCreatedById(@Param("createdById") Integer createdById, Pageable pageable);

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
           "WHERE s.taskId = :taskId")
    Page<Submission> findByTaskId(@Param("taskId") Integer taskId, Pageable pageable);

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
           "WHERE LOWER(s.createdBy.nickname) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Submission> findByCreatedByNicknameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.createdBy LEFT JOIN FETCH s.task " +
           "WHERE LOWER(s.task.name) LIKE LOWER(CONCAT('%', :taskName, '%'))")
    Page<Submission> findByTaskNameContaining(@Param("taskName") String taskName, Pageable pageable);

    List<Submission> findByCreatedById(Integer createdById);

    List<Submission> findByTaskId(Integer taskId);
}
