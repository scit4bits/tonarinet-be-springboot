package org.scit4bits.tonarinetserver.controller;

import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@Slf4j
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDTO> getSpecificArticle(@AuthenticationPrincipal User user,
        @PathVariable("articleId") Integer articleId
    ){
        ArticleDTO article = articleService.readArticle(user, articleId);

        if(article == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(article);
    }

    @GetMapping("/{articleId}/view")
    public ResponseEntity<SimpleResponse> getincreaseArticleViews(@AuthenticationPrincipal User user,
        @PathVariable("articleId") Integer articleId
    ){
        boolean success = articleService.increaseArticleViews(user, articleId);

        if(!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new SimpleResponse("Views increased successfully"));
    }
    
}
