package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지역 엔티티
 */
@Entity
@Table(name = "region")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    /** 지역 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 국가 코드 */
    @Column(name = "country_code", length = 5, nullable = false)
    private String countryCode;

    /** 카테고리 1 */
    @Column(name = "category1", length = 20)
    private String category1;

    /** 카테고리 2 */
    @Column(name = "category2", length = 20)
    private String category2;

    /** 카테고리 3 */
    @Column(name = "category3", length = 20)
    private String category3;

    /** 카테고리 4 */
    @Column(name = "category4", length = 20)
    private String category4;

    /** 위도 */
    @Column(name = "latitude")
    private Double latitude;

    /** 경도 */
    @Column(name = "longitude")
    private Double longitude;

    /** 반경 */
    @Column(name = "radius")
    private Integer radius;

    /** 국가 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;
}
