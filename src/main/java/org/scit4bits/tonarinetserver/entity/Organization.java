package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 60, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "country_code", length = 5, nullable = false)
    private String countryCode;

    @Column(name = "type", columnDefinition = "ENUM('SCHOOL', 'COMPANY')", nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Team> teams;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<TaskGroup> taskGroups;

    @ManyToMany(mappedBy = "organizations")
    private List<User> users;
}
