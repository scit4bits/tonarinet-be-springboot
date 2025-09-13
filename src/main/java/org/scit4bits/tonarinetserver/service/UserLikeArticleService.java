package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Article;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserLikeArticle;
import org.scit4bits.tonarinetserver.repository.ArticleRepository;
import org.scit4bits.tonarinetserver.repository.UserLikeArticleRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLikeArticleService {

    private final UserLikeArticleRepository userLikeArticleRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    /**
     * 게시글에 좋아요 추가
     */
    public boolean likeArticle(Integer userId, Integer articleId) {
        // 이미 좋아요한 경우 false 반환
        if (userLikeArticleRepository.existsByUserIdAndArticleId(userId, articleId)) {
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));

        UserLikeArticle userLikeArticle = UserLikeArticle.builder()
                .id(UserLikeArticle.UserLikeArticleId.builder()
                        .userId(userId)
                        .articleId(articleId)
                        .build())
                .user(user)
                .article(article)
                .build();

        userLikeArticleRepository.save(userLikeArticle);
        return true;
    }

    /**
     * 게시글 좋아요 취소
     */
    public boolean unlikeArticle(Integer userId, Integer articleId) {
        Optional<UserLikeArticle> userLikeArticle = userLikeArticleRepository
                .findByUserIdAndArticleId(userId, articleId);

        if (userLikeArticle.isPresent()) {
            userLikeArticleRepository.delete(userLikeArticle.get());
            return true;
        }

        return false;
    }

    /**
     * 좋아요 상태 토글 (좋아요가 있으면 취소, 없으면 추가)
     */
    public boolean toggleLike(Integer userId, Integer articleId) {
        if (userLikeArticleRepository.existsByUserIdAndArticleId(userId, articleId)) {
            return !unlikeArticle(userId, articleId); // 좋아요 취소 성공하면 false 반환
        } else {
            return likeArticle(userId, articleId); // 좋아요 추가 성공하면 true 반환
        }
    }

    /**
     * 사용자가 게시글에 좋아요했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Integer userId, Integer articleId) {
        return userLikeArticleRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    /**
     * 게시글의 좋아요 수 조회
     */
    @Transactional(readOnly = true)
    public Integer getLikeCount(Integer articleId) {
        return userLikeArticleRepository.countByArticleId(articleId);
    }

    /**
     * 사용자가 좋아요한 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserLikeArticle> getLikedArticlesByUser(Integer userId) {
        return userLikeArticleRepository.findByUserId(userId);
    }

    /**
     * 게시글에 좋아요한 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserLikeArticle> getUsersWhoLikedArticle(Integer articleId) {
        return userLikeArticleRepository.findByArticleId(articleId);
    }
}
