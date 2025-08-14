package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TownReview")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TownReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;
    
    @Column(name = "created_by", nullable = false)
    private Integer createdById;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "transportation", nullable = false)
    private Integer transportation;
    
    @Column(name = "safety", nullable = false)
    private Integer safety;
    
    @Column(name = "infra", nullable = false)
    private Integer infra;
    
    @Column(name = "population", nullable = false)
    private Integer population;
    
    @Column(name = "education", nullable = false)
    private Integer education;
    
    @Column(name = "region_id", nullable = false)
    private Integer regionId;
    
    @Column(name = "country_code", length = 5, nullable = false)
    private String countryCode;
    
    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", insertable = false, updatable = false)
    private Region region;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;
}
