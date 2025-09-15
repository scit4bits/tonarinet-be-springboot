package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.UserLikeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자-게시글 좋아요(UserLikeArticle) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface UserLikeArticleRepository extends JpaRepository<UserLikeArticle, UserLikeArticle.UserLikeArticleId> {

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.
     * @param userId 사용자 ID
     * @param articleId 게시글 ID
     * @return {@link Optional}{@link UserLikeArticle}
     */
    @Query("SELECT ula FROM UserLikeArticle ula WHERE ula.user.id = :userId AND ula.article.id = :articleId")
    Optional<UserLikeArticle> findByUserIdAndArticleId(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    /**
     * 특정 게시글의 좋아요 수를 계산합니다.
     * @param articleId 게시글 ID
     * @return 좋아요 수
     */
    @Query("SELECT COUNT(ula) FROM UserLikeArticle ula WHERE ula.article.id = :articleId")
    Integer countByArticleId(@Param("articleId") Integer articleId);

    /**
     * 특정 사용자가 좋아요한 모든 게시글-사용자 관계를 조회합니다.
     * @param userId 사용자 ID
     * @return UserLikeArticle 리스트
     */
    @Query("SELECT ula FROM UserLikeArticle ula WHERE ula.user.id = :userId")
    List<UserLikeArticle> findByUserId(@Param("userId") Integer userId);

    /**
     * 특정 게시글에 좋아요를 누른 모든 사용자-게시글 관계를 조회합니다.
     * @param articleId 게시글 ID
     * @return UserLikeArticle 리스트
     */
    @Query("SELECT ula FROM UserLikeArticle ula WHERE ula.article.id = :articleId")
    List<UserLikeArticle> findByArticleId(@Param("articleId") Integer articleId);

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 여부를 확인합니다.
     * @param userId 사용자 ID
     * @param articleId 게시글 ID
     * @return 좋아요를 눌렀으면 true, 아니면 false
     */
    @Query("SELECT CASE WHEN COUNT(ula) > 0 THEN true ELSE false END FROM UserLikeArticle ula WHERE ula.user.id = :userId AND ula.article.id = :articleId")
    boolean existsByUserIdAndArticleId(@Param("userId") Integer userId, @Param("articleId") Integer articleId);
}
