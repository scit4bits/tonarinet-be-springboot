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
    
    // 게시판의 모든 게시글 (counsel 카테고리 제외)
    List<Article> findAllByBoardIdAndCategoryNot(Integer boardId, String category);
    
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
    
    // 게시판 ID로 검색 (counsel 카테고리 제외)
    Page<Article> findByBoardIdAndCategoryNot(Integer boardId, String category, Pageable pageable);

    
    
    // 특정 게시판의 게시물들 (최신 순)
    List<Article> findByBoardIdOrderByCreatedAtDesc(Integer boardId);
    
    // 특정 게시판의 게시물들 (최신 순, counsel 카테고리 제외)
    List<Article> findByBoardIdAndCategoryNotOrderByCreatedAtDesc(Integer boardId, String category);
    
    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT a FROM Article a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Article> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
    
    // 특정 게시판에서의 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND (" +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> findByBoardIdAndAllFieldsContaining(@Param("boardId") Integer boardId, @Param("search") String search, Pageable pageable);
    
    // 특정 게시판에서의 전체 검색을 위한 커스텀 쿼리 (counsel 카테고리 제외)
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != 'counsel' AND (" +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> findByBoardIdAndAllFieldsContainingExcludingCounsel(@Param("boardId") Integer boardId, @Param("search") String search, Pageable pageable);
    
    // 특정 게시판의 제목으로 검색
    Page<Article> findByBoardIdAndTitleContainingIgnoreCase(Integer boardId, String title, Pageable pageable);
    
    // 특정 게시판의 제목으로 검색 (counsel 카테고리 제외)
    Page<Article> findByBoardIdAndTitleContainingIgnoreCaseAndCategoryNot(Integer boardId, String title, String category, Pageable pageable);
    
    // 특정 게시판의 내용으로 검색
    Page<Article> findByBoardIdAndContentsContainingIgnoreCase(Integer boardId, String contents, Pageable pageable);
    
    // 특정 게시판의 내용으로 검색 (counsel 카테고리 제외)
    Page<Article> findByBoardIdAndContentsContainingIgnoreCaseAndCategoryNot(Integer boardId, String contents, String category, Pageable pageable);
    
    // 특정 게시판의 카테고리로 검색
    Page<Article> findByBoardIdAndCategoryContainingIgnoreCase(Integer boardId, String category, Pageable pageable);
    
    // 특정 게시판의 카테고리로 검색 (counsel 카테고리 제외)
    Page<Article> findByBoardIdAndCategoryContainingIgnoreCaseAndCategoryNot(Integer boardId, String categorySearch, String excludeCategory, Pageable pageable);
    
    // 특정 게시판의 생성자 ID로 검색
    Page<Article> findByBoardIdAndCreatedById(Integer boardId, Integer createdById, Pageable pageable);
    
    // 특정 게시판의 생성자 ID로 검색 (counsel 카테고리 제외)
    Page<Article> findByBoardIdAndCreatedByIdAndCategoryNot(Integer boardId, Integer createdById, String category, Pageable pageable);
    
    // 특정 게시판에서 특정 카테고리 제외하고 좋아요 10개 이상인 게시글 검색
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != :category AND SIZE(a.likedByUsers) >= 10")
    Page<Article> findByBoardIdAndCategoryNotAndLikedByUsersCountGreaterThanEqual(@Param("boardId") Integer boardId, @Param("category") String category, Pageable pageable);
    
    // 특정 게시판에서 특정 카테고리 제외하고 좋아요 10개 이상인 게시글 검색 (List 반환)
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != :category AND SIZE(a.likedByUsers) >= 10 ORDER BY a.createdAt DESC")
    List<Article> findByBoardIdAndCategoryNotAndLikedByUsersCountGreaterThanEqualOrderByCreatedAtDesc(@Param("boardId") Integer boardId, @Param("category") String category);
    
    // Alternative approach using JOIN and COUNT for better performance
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != :category AND " +
           "(SELECT COUNT(ula) FROM UserLikeArticle ula WHERE ula.article = a) >= 10")
    Page<Article> findByBoardIdAndCategoryNotWithLikeCountGreaterThanEqual(@Param("boardId") Integer boardId, @Param("category") String category, Pageable pageable);
    
    // Category-specific search methods
    Page<Article> findByBoardIdAndCategory(Integer boardId, String category, Pageable pageable);
    
    Page<Article> findByBoardIdAndCategoryAndTitleContainingIgnoreCase(Integer boardId, String category, String title, Pageable pageable);
    
    Page<Article> findByBoardIdAndCategoryAndContentsContainingIgnoreCase(Integer boardId, String category, String contents, Pageable pageable);
    
    Page<Article> findByBoardIdAndCategoryAndCreatedById(Integer boardId, String category, Integer createdById, Pageable pageable);
    
    // Custom query for category-specific all fields search
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category = :category AND (" +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> findByBoardIdAndCategoryAndAllFieldsContaining(@Param("boardId") Integer boardId, @Param("category") String category, @Param("search") String search, Pageable pageable);
}
