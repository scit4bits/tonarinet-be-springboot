package org.scit4bits.tonarinetserver.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "townreview")
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
    
    @Column(name = "longitude", nullable = false)
    private String longitude;
    
    @Column(name = "latitude", nullable = false)
    private String latitude;
    
    @Column(name = "radius", nullable = false)
    private Integer radius;
    
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
