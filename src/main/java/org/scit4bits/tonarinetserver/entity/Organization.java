package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 조직 엔티티
 */
@Entity
@Table(name = "Organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    /** 조직 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 조직 이름 */
    @Column(name = "name", length = 60, nullable = false, unique = true)
    private String name;

    /** 조직 설명 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 국가 코드 */
    @Column(name = "country_code", length = 5, nullable = false)
    private String countryCode;

    /** 조직 타입 */
    @Column(name = "type", columnDefinition = "ENUM('SCHOOL', 'COMPANY')", nullable = false)
    private String type;

    /** 국가 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;

    /** 팀 목록 */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Team> teams;

    /** 과제 그룹 목록 */
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<TaskGroup> taskGroups;

    /** 조직에 속한 사용자 목록 */
    @ManyToMany(mappedBy = "organizations")
    private List<User> users;
}
