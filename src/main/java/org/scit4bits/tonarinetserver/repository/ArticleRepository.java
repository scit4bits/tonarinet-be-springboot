package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시글(Article) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    /**
     * 특정 게시판의 모든 게시글을 조회합니다.
     * @param boardId 게시판 ID
     * @return 게시글 리스트
     */
    List<Article> findAllByBoardId(Integer boardId);

    /**
     * 특정 카테고리를 제외하고 특정 게시판의 모든 게시글을 조회합니다.
     * @param boardId 게시판 ID
     * @param category 제외할 카테고리
     * @return 게시글 리스트
     */
    List<Article> findAllByBoardIdAndCategoryNot(Integer boardId, String category);

    /**
     * ID로 게시글을 페이징하여 조회합니다.
     * @param id 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findById(Integer id, Pageable pageable);

    /**
     * 제목에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param title 검색할 제목 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 내용에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByContentsContainingIgnoreCase(String contents, Pageable pageable);

    /**
     * 카테고리에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param category 검색할 카테고리 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    /**
     * 작성자 ID로 게시글을 페이징하여 조회합니다.
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByCreatedById(Integer createdById, Pageable pageable);

    /**
     * 게시판 ID로 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardId(Integer boardId, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 게시판 ID로 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param category 제외할 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCategoryNot(Integer boardId, String category, Pageable pageable);


    /**
     * 특정 게시판의 게시글들을 최신순으로 조회합니다.
     * @param boardId 게시판 ID
     * @return 최신순으로 정렬된 게시글 리스트
     */
    List<Article> findByBoardIdOrderByCreatedAtDesc(Integer boardId);

    /**
     * 특정 카테고리를 제외하고 특정 게시판의 게시글들을 최신순으로 조회합니다.
     * @param boardId 게시판 ID
     * @param category 제외할 카테고리
     * @return 최신순으로 정렬된 게시글 리스트
     */
    List<Article> findByBoardIdAndCategoryNotOrderByCreatedAtDesc(Integer boardId, String category);

    /**
     * 모든 필드(제목, 내용, 카테고리)에서 검색어와 일치하는 게시글을 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    @Query("SELECT a FROM Article a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Article> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    /**
     * 특정 게시판 내에서 모든 필드(제목, 내용, 카테고리)에서 검색어와 일치하는 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND (" +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> findByBoardIdAndAllFieldsContaining(@Param("boardId") Integer boardId, @Param("search") String search, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 특정 게시판 내에서 모든 필드(제목, 내용, 카테고리)에서 검색어와 일치하는 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != 'counsel' AND (" +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> findByBoardIdAndAllFieldsContainingExcludingCounsel(@Param("boardId") Integer boardId, @Param("search") String search, Pageable pageable);

    /**
     * 특정 게시판에서 제목에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param title 검색할 제목 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndTitleContainingIgnoreCase(Integer boardId, String title, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 특정 게시판에서 제목에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param title 검색할 제목 문자열
     * @param category 제외할 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndTitleContainingIgnoreCaseAndCategoryNot(Integer boardId, String title, String category, Pageable pageable);

    /**
     * 특정 게시판에서 내용에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndContentsContainingIgnoreCase(Integer boardId, String contents, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 특정 게시판에서 내용에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param contents 검색할 내용 문자열
     * @param category 제외할 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndContentsContainingIgnoreCaseAndCategoryNot(Integer boardId, String contents, String category, Pageable pageable);

    /**
     * 특정 게시판에서 카테고리에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param category 검색할 카테고리 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCategoryContainingIgnoreCase(Integer boardId, String category, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 특정 게시판에서 카테고리에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param categorySearch 검색할 카테고리 문자열
     * @param excludeCategory 제외할 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCategoryContainingIgnoreCaseAndCategoryNot(Integer boardId, String categorySearch, String excludeCategory, Pageable pageable);

    /**
     * 특정 게시판에서 작성자 ID로 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCreatedById(Integer boardId, Integer createdById, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 특정 게시판에서 작성자 ID로 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param createdById 작성자 ID
     * @param category 제외할 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCreatedByIdAndCategoryNot(Integer boardId, Integer createdById, String category, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 특정 게시판에서 좋아요가 5개 이상인 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param category 제외할 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != :category AND SIZE(a.likedByUsers) >= 5")
    Page<Article> findByBoardIdAndCategoryNotAndLikedByUsersCountGreaterThanEqual(@Param("boardId") Integer boardId, @Param("category") String category, Pageable pageable);

    /**
     * 특정 카테고리를 제외하고 특정 게시판에서 좋아요가 10개 이상인 게시글을 최신순으로 조회합니다.
     * @param boardId 게시판 ID
     * @param category 제외할 카테고리
     * @return 게시글 리스트
     */
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != :category AND SIZE(a.likedByUsers) >= 10 ORDER BY a.createdAt DESC")
    List<Article> findByBoardIdAndCategoryNotAndLikedByUsersCountGreaterThanEqualOrderByCreatedAtDesc(@Param("boardId") Integer boardId, @Param("category") String category);

    /**
     * JOIN과 COUNT를 사용하여 성능을 개선한, 특정 카테고리를 제외하고 특정 게시판에서 좋아요가 10개 이상인 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param category 제외할 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category != :category AND " +
            "(SELECT COUNT(ula) FROM UserLikeArticle ula WHERE ula.article = a) >= 10")
    Page<Article> findByBoardIdAndCategoryNotWithLikeCountGreaterThanEqual(@Param("boardId") Integer boardId, @Param("category") String category, Pageable pageable);

    /**
     * 특정 게시판의 특정 카테고리에 속하는 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param category 카테고리
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCategory(Integer boardId, String category, Pageable pageable);

    /**
     * 특정 게시판의 특정 카테고리에서 제목에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param category 카테고리
     * @param title 검색할 제목 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCategoryAndTitleContainingIgnoreCase(Integer boardId, String category, String title, Pageable pageable);

    /**
     * 특정 게시판의 특정 카테고리에서 내용에 특정 문자열을 포함하는 게시글을 페이징하여 조회합니다. (대소문자 무시)
     * @param boardId 게시판 ID
     * @param category 카테고리
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCategoryAndContentsContainingIgnoreCase(Integer boardId, String category, String contents, Pageable pageable);

    /**
     * 특정 게시판의 특정 카테고리에서 작성자 ID로 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param category 카테고리
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    Page<Article> findByBoardIdAndCategoryAndCreatedById(Integer boardId, String category, Integer createdById, Pageable pageable);

    /**
     * 특정 게시판의 특정 카테고리 내에서 모든 필드(제목, 내용)에서 검색어와 일치하는 게시글을 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param category 카테고리
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 게시글
     */
    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.category = :category AND (" +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.contents) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> findByBoardIdAndCategoryAndAllFieldsContaining(@Param("boardId") Integer boardId, @Param("category") String category, @Param("search") String search, Pageable pageable);
}
