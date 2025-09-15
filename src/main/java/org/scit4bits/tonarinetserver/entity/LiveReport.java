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
 * 실시간 제보 엔티티
 */
@Entity
@Table(name = "livereport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LiveReport {

    /** 제보 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 내용 */
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    /** 좋아요 수 */
    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /** 생성일 */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 작성자 ID */
    @Column(name = "created_by", nullable = false)
    private Integer createdById;

    /** 경도 */
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /** 위도 */
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;
}
