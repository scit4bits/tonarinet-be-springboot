package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 태그 엔티티
 */
@Entity
@Table(name = "tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    /** 복합 키 */
    @EmbeddedId
    private TagId id;

    /** 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    private Article article;

    /**
     * 태그 ID 복합 키 클래스
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TagId {

        /** 게시글 ID */
        @Column(name = "article_id")
        private Integer articleId;

        /** 태그 이름 */
        @Column(name = "tag_name", length = 20)
        private String tagName;
    }
}

