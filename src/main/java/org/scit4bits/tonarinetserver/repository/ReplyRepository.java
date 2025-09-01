package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    
    // ID로 검색
    Page<Reply> findById(Integer id, Pageable pageable);
    
    // 내용으로 검색
    Page<Reply> findByContentsContainingIgnoreCase(String contents, Pageable pageable);
    
    // 생성자 ID로 검색
    Page<Reply> findByCreatedById(Integer createdById, Pageable pageable);
    
    // 게시물 ID로 검색
    Page<Reply> findByArticleId(Integer articleId, Pageable pageable);
    
    // 특정 게시물의 댓글들 (생성일 순)
    List<Reply> findByArticleIdOrderByCreatedAtAsc(Integer articleId);
    
    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT r FROM Reply r WHERE " +
           "LOWER(r.contents) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Reply> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
