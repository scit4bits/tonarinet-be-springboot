package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    List<Article> findAllByBoardId(Integer boardId);
    
    // ID로 검색
    Page<Article> findById(Integer id, Pageable pageable);
    
    // 제목으로 검색
    Page<Article> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // 내용으로 검색
    Page<Article> findByContentsContainingIgnoreCase(String contents, Pageable pageable);
    
    // 카테고리로 검색
    Page<Article> findByCategoryContainingIgnoreCase(String category, Pageable pageable);
    
    // 생성자 ID로 검색
    Page<Article> findByCreatedById(Integer createdById, Pageable pageable);
    
    // 게시판 ID로 검색
    Page<Article> findByBoardId(Integer boardId, Pageable pageable);
    
    // 특정 게시판의 게시물들 (최신 순)
    List<Article> findByBoardIdOrderByCreatedAtDesc(Integer boardId);
    
    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT a FROM Article a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Article> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
