package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "userlikearticle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLikeArticle {

    @EmbeddedId
    private UserLikeArticleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    private Article article;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserLikeArticleId {

        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "article_id")
        private Integer articleId;
    }
}
