package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 동네 리뷰 엔티티
 */
@Entity
@Table(name = "townreview")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TownReview {

    /** 리뷰 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 내용 */
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    /** 작성자 ID */
    @Column(name = "created_by", nullable = false)
    private Integer createdById;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 교통 평점 */
    @Column(name = "transportation", nullable = false)
    private Integer transportation;

    /** 안전 평점 */
    @Column(name = "safety", nullable = false)
    private Integer safety;

    /** 인프라 평점 */
    @Column(name = "infra", nullable = false)
    private Integer infra;

    /** 인구 평점 */
    @Column(name = "population", nullable = false)
    private Integer population;

    /** 교육 평점 */
    @Column(name = "education", nullable = false)
    private Integer education;

    /** 지역 ID */
    @Column(name = "region_id", nullable = false)
    private Integer regionId;

    /** 국가 코드 */
    @Column(name = "country_code", length = 5, nullable = false)
    private String countryCode;

    /** 좋아요 수 */
    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;

    /** 지역 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", insertable = false, updatable = false)
    private Region region;

    /** 국가 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;
}
