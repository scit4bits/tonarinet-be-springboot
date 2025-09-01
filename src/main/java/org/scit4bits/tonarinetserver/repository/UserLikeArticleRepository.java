package org.scit4bits.tonarinetserver.repository;

import java.util.List;
import java.util.Optional;

import org.scit4bits.tonarinetserver.entity.UserLikeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLikeArticleRepository extends JpaRepository<UserLikeArticle, UserLikeArticle.UserLikeArticleId> {
    
    // 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    @Query("SELECT ula FROM UserLikeArticle ula WHERE ula.user.id = :userId AND ula.article.id = :articleId")
    Optional<UserLikeArticle> findByUserIdAndArticleId(Integer userId, Integer articleId);
    
    // 특정 게시글의 좋아요 수
    @Query("SELECT COUNT(ula) FROM UserLikeArticle ula WHERE ula.article.id = :articleId")
    Long countByArticleId(Integer articleId);
    
    // 사용자가 좋아요한 게시글 목록
    @Query("SELECT ula FROM UserLikeArticle ula WHERE ula.user.id = :userId")
    List<UserLikeArticle> findByUserId(Integer userId);
    
    // 특정 게시글에 좋아요한 사용자 목록
    @Query("SELECT ula FROM UserLikeArticle ula WHERE ula.article.id = :articleId")
    List<UserLikeArticle> findByArticleId(Integer articleId);
    
    // 사용자가 특정 게시글에 좋아요를 눌렀는지 여부
    @Query("SELECT CASE WHEN COUNT(ula) > 0 THEN true ELSE false END FROM UserLikeArticle ula WHERE ula.user.id = :userId AND ula.article.id = :articleId")
    boolean existsByUserIdAndArticleId(Integer userId, Integer articleId);
}
