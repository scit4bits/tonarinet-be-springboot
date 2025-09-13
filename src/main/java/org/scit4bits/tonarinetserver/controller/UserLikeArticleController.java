package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserLikeArticle;
import org.scit4bits.tonarinetserver.service.UserLikeArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글 좋아요 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "UserLikeArticle", description = "게시글 좋아요 API")
public class UserLikeArticleController {

    private final UserLikeArticleService userLikeArticleService;

    /**
     * 특정 게시글에 '좋아요'를 추가합니다.
     * @param articleId '좋아요'를 추가할 게시글 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 업데이트된 '좋아요' 수
     */
    @PostMapping("/{articleId}")
    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
    public ResponseEntity<Integer> likeArticle(
            @PathVariable("articleId") Integer articleId,
            @AuthenticationPrincipal User user) {

        try {
            boolean success = userLikeArticleService.likeArticle(user.getId(), articleId);

            if (success) {
                Integer likeCount = userLikeArticleService.getLikeCount(articleId);
                return ResponseEntity.ok(likeCount);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 게시글의 '좋아요'를 취소합니다.
     * @param articleId '좋아요'를 취소할 게시글 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 업데이트된 '좋아요' 수
     */
    @DeleteMapping("/{articleId}")
    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글의 좋아요를 취소합니다.")
    public ResponseEntity<Integer> unlikeArticle(
            @PathVariable("articleId") Integer articleId,
            @AuthenticationPrincipal User user) {

        try {
            boolean success = userLikeArticleService.unlikeArticle(user.getId(), articleId);

            if (success) {
                Integer likeCount = userLikeArticleService.getLikeCount(articleId);
                return ResponseEntity.ok(likeCount);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 게시글 '좋아요' 상태를 토글합니다.
     * @param articleId '좋아요' 상태를 토글할 게시글 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 업데이트된 '좋아요' 수
     */
    @PostMapping("/{articleId}/toggle")
    @Operation(summary = "게시글 좋아요 토글", description = "게시글 좋아요 상태를 토글합니다.")
    public ResponseEntity<Integer> toggleLike(
            @PathVariable("articleId") Integer articleId,
            @AuthenticationPrincipal User user) {

        try {
            userLikeArticleService.toggleLike(user.getId(), articleId);
            Integer likeCount = userLikeArticleService.getLikeCount(articleId);
            return ResponseEntity.ok(likeCount);
        } catch (Exception e) {
            log.debug("Error toggling like status for articleId: {}", articleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 게시글의 '좋아요' 수를 조회합니다.
     * @param articleId '좋아요' 수를 조회할 게시글 ID
     * @return '좋아요' 수
     */
    @GetMapping("/{articleId}/count")
    @Operation(summary = "게시글 좋아요 수 조회", description = "특정 게시글의 좋아요 수를 조회합니다.")
    public ResponseEntity<Integer> getLikeCount(@PathVariable("articleId") Integer articleId) {
        try {
            Integer likeCount = userLikeArticleService.getLikeCount(articleId);
            return ResponseEntity.ok(likeCount);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자가 특정 게시글에 '좋아요'를 눌렀는지 확인합니다.
     * @param articleId 확인할 게시글 ID
     * @param user 현재 로그인한 사용자 정보
     * @return '좋아요' 여부
     */
    @GetMapping("/{articleId}/status")
    @Operation(summary = "좋아요 상태 확인", description = "사용자가 특정 게시글에 좋아요했는지 확인합니다.")
    public ResponseEntity<Boolean> getLikeStatus(
            @PathVariable("articleId") Integer articleId,
            @AuthenticationPrincipal User user) {

        try {
            boolean isLiked = userLikeArticleService.isLikedByUser(user.getId(), articleId);
            return ResponseEntity.ok(isLiked);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 현재 로그인한 사용자가 '좋아요'한 게시글 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return '좋아요'한 게시글 목록
     */
    @GetMapping("/liked-by-user")
    @Operation(summary = "사용자가 좋아요한 게시글 목록", description = "인증된 사용자가 좋아요한 게시글 목록을 조회합니다.")
    public ResponseEntity<List<UserLikeArticle>> getLikedArticlesByUser(@AuthenticationPrincipal User user) {
        try {
            List<UserLikeArticle> likedArticles = userLikeArticleService.getLikedArticlesByUser(user.getId());
            return ResponseEntity.ok(likedArticles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
