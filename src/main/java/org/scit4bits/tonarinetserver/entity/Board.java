package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "board")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Column(name = "org_id")
    private Integer orgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Article> articles;
}
