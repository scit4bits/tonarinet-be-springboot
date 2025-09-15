package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 국가 엔티티
 */
@Entity
@Table(name = "country")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    /** 국가 코드 */
    @Id
    @Column(name = "country_code", length = 5)
    private String countryCode;

    /** 국가 이름 */
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    /** 국가 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 조직 목록 */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<Organization> organizations;

    /** 게시판 목록 */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<Board> boards;

    /** 지역 목록 */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<Region> regions;

    /** 동네 리뷰 목록 */
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<TownReview> townReviews;

    /** 해당 국가 국적의 사용자 목록 */
    @OneToMany(mappedBy = "nationality", cascade = CascadeType.ALL)
    private List<User> nationals;

    /** 국가에 속한 사용자 목록 */
    @ManyToMany(mappedBy = "countries")
    private List<User> users;
}
