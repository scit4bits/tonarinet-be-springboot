package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "country")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<Organization> organizations;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<Board> boards;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<Region> regions;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private List<TownReview> townReviews;

    @OneToMany(mappedBy = "nationality", cascade = CascadeType.ALL)
    private List<User> nationals;

    @ManyToMany(mappedBy = "countries")
    private List<User> users;
}
