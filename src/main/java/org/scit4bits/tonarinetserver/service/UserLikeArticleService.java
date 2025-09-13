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

/**
 * 게시글 좋아요 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserLikeArticleService {

    private final UserLikeArticleRepository userLikeArticleRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    /**
     * 특정 게시글에 좋아요를 추가합니다.
     * @param userId 사용자의 ID
     * @param articleId 게시글의 ID
     * @return 작업 성공 시 true, 이미 좋아요를 누른 경우 false
     */
    public boolean likeArticle(Integer userId, Integer articleId) {
        // 이미 좋아요를 눌렀는지 확인
        if (userLikeArticleRepository.existsByUserIdAndArticleId(userId, articleId)) {
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + articleId));

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
     * 특정 게시글의 좋아요를 취소합니다.
     * @param userId 사용자의 ID
     * @param articleId 게시글의 ID
     * @return 작업 성공 시 true, 좋아요를 누르지 않은 경우 false
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
     * 게시글의 좋아요 상태를 토글합니다. (좋아요 -> 취소, 취소 -> 좋아요)
     * @param userId 사용자의 ID
     * @param articleId 게시글의 ID
     * @return 토글 후의 좋아요 상태 (true: 좋아요, false: 취소)
     */
    public boolean toggleLike(Integer userId, Integer articleId) {
        if (userLikeArticleRepository.existsByUserIdAndArticleId(userId, articleId)) {
            unlikeArticle(userId, articleId);
            return false; // 좋아요 취소
        } else {
            likeArticle(userId, articleId);
            return true; // 좋아요 추가
        }
    }

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.
     * @param userId 사용자의 ID
     * @param articleId 게시글의 ID
     * @return 좋아요를 눌렀으면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Integer userId, Integer articleId) {
        return userLikeArticleRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    /**
     * 특정 게시글의 좋아요 수를 조회합니다.
     * @param articleId 게시글의 ID
     * @return 좋아요 수
     */
    @Transactional(readOnly = true)
    public Integer getLikeCount(Integer articleId) {
        return userLikeArticleRepository.countByArticleId(articleId);
    }

    /**
     * 특정 사용자가 좋아요한 모든 게시글 목록을 조회합니다.
     * @param userId 사용자의 ID
     * @return 좋아요한 게시글과 사용자 관계 목록
     */
    @Transactional(readOnly = true)
    public List<UserLikeArticle> getLikedArticlesByUser(Integer userId) {
        return userLikeArticleRepository.findByUserId(userId);
    }

    /**
     * 특정 게시글에 좋아요를 누른 모든 사용자 목록을 조회합니다.
     * @param articleId 게시글의 ID
     * @return 해당 게시글을 좋아요한 사용자와 게시글 관계 목록
     */
    @Transactional(readOnly = true)
    public List<UserLikeArticle> getUsersWhoLikedArticle(Integer articleId) {
        return userLikeArticleRepository.findByArticleId(articleId);
    }
}
