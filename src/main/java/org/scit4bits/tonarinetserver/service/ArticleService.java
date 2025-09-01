package org.scit4bits.tonarinetserver.service;

import org.scit4bits.tonarinetserver.entity.Article;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public Article getArticleById(Integer articleId) {
        return articleRepository.findById(articleId).orElse(null);
    }

    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    public Article updateArticle(Integer articleId, Article article) {
        // 
        return articleRepository.save(article);
    }

    public void deleteArticle(Integer articleId) {
        articleRepository.deleteById(articleId);
    }

    public void addReplyToArticle(Integer articleId, User replyUser){
        
    }
}
