package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자-게시글 좋아요 엔티티
 */
@Entity
@Table(name = "userlikearticle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLikeArticle {

    /** 복합 키 */
    @EmbeddedId
    private UserLikeArticleId id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /** 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    private Article article;

    /**
     * 사용자-게시글 좋아요 ID 복합 키 클래스
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserLikeArticleId {

        /** 사용자 ID */
        @Column(name = "user_id")
        private Integer userId;

        /** 게시글 ID */
        @Column(name = "article_id")
        private Integer articleId;
    }
}

