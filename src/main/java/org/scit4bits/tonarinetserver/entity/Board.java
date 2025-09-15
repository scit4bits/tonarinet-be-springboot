package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시판 엔티티
 */
@Entity
@Table(name = "board")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    /** 게시판 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 게시판 제목 */
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    /** 게시판 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 국가 코드 */
    @Column(name = "country_code", length = 5)
    private String countryCode;

    /** 조직 ID */
    @Column(name = "org_id")
    private Integer orgId;

    /** 국가 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;

    /** 조직 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    /** 게시글 목록 */
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Article> articles;
}
