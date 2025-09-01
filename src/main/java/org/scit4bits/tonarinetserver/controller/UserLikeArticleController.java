package org.scit4bits.tonarinetserver.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scit4bits.tonarinetserver.entity.UserLikeArticle;
import org.scit4bits.tonarinetserver.service.UserLikeArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "UserLikeArticle", description = "게시글 좋아요 API")
public class UserLikeArticleController {
    
    private final UserLikeArticleService userLikeArticleService;
    
    @PostMapping("/{articleId}/like")
    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
    public ResponseEntity<Map<String, Object>> likeArticle(
            @PathVariable Integer articleId,
            @RequestParam Integer userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = userLikeArticleService.likeArticle(userId, articleId);
            
            if (success) {
                Long likeCount = userLikeArticleService.getLikeCount(articleId);
                response.put("success", true);
                response.put("message", "좋아요가 추가되었습니다.");
                response.put("likeCount", likeCount);
                response.put("isLiked", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "이미 좋아요한 게시글입니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/{articleId}/like")
    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글의 좋아요를 취소합니다.")
    public ResponseEntity<Map<String, Object>> unlikeArticle(
            @PathVariable Integer articleId,
            @RequestParam Integer userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = userLikeArticleService.unlikeArticle(userId, articleId);
            
            if (success) {
                Long likeCount = userLikeArticleService.getLikeCount(articleId);
                response.put("success", true);
                response.put("message", "좋아요가 취소되었습니다.");
                response.put("likeCount", likeCount);
                response.put("isLiked", false);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "좋아요하지 않은 게시글입니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/{articleId}/like/toggle")
    @Operation(summary = "게시글 좋아요 토글", description = "게시글 좋아요 상태를 토글합니다.")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Integer articleId,
            @RequestParam Integer userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isLiked = userLikeArticleService.toggleLike(userId, articleId);
            Long likeCount = userLikeArticleService.getLikeCount(articleId);
            
            response.put("success", true);
            response.put("message", isLiked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.");
            response.put("likeCount", likeCount);
            response.put("isLiked", isLiked);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/{articleId}/like/count")
    @Operation(summary = "게시글 좋아요 수 조회", description = "특정 게시글의 좋아요 수를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getLikeCount(@PathVariable Integer articleId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long likeCount = userLikeArticleService.getLikeCount(articleId);
            response.put("success", true);
            response.put("articleId", articleId);
            response.put("likeCount", likeCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 수 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/{articleId}/like/status")
    @Operation(summary = "좋아요 상태 확인", description = "사용자가 특정 게시글에 좋아요했는지 확인합니다.")
    public ResponseEntity<Map<String, Object>> getLikeStatus(
            @PathVariable Integer articleId,
            @RequestParam Integer userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isLiked = userLikeArticleService.isLikedByUser(userId, articleId);
            Long likeCount = userLikeArticleService.getLikeCount(articleId);
            
            response.put("success", true);
            response.put("articleId", articleId);
            response.put("userId", userId);
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/liked-by-user/{userId}")
    @Operation(summary = "사용자가 좋아요한 게시글 목록", description = "특정 사용자가 좋아요한 게시글 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getLikedArticlesByUser(@PathVariable Integer userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<UserLikeArticle> likedArticles = userLikeArticleService.getLikedArticlesByUser(userId);
            
            response.put("success", true);
            response.put("userId", userId);
            response.put("likedArticles", likedArticles);
            response.put("totalCount", likedArticles.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요한 게시글 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
