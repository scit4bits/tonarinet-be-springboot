package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 게시글 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    /**
     * 특정 게시글을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param articleId 조회할 게시글 ID
     * @return ArticleDTO 형태의 게시글 정보
     */
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDTO> getSpecificArticle(@AuthenticationPrincipal User user,
                                                         @PathVariable("articleId") Integer articleId
    ) {
        // articleService를 통해 게시글 정보를 조회합니다.
        ArticleDTO article = articleService.readArticle(user, articleId);

        if (article == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(article);
    }

    /**
     * 특정 게시글의 조회수를 증가시킵니다.
     * @param user 현재 로그인한 사용자 정보
     * @param articleId 조회수를 증가시킬 게시글 ID
     * @return 조회수 증가 성공 여부
     */
    @GetMapping("/{articleId}/view")
    public ResponseEntity<SimpleResponse> getincreaseArticleViews(@AuthenticationPrincipal User user,
                                                                  @PathVariable("articleId") Integer articleId
    ) {
        // articleService를 통해 게시글 조회수를 증가시킵니다.
        boolean success = articleService.increaseArticleViews(user, articleId);

        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new SimpleResponse("Views increased successfully"));
    }

}
