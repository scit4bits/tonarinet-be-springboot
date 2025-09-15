package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 팀 엔티티
 */
@Entity
@Table(name = "team")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    /** 팀 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 팀 이름 */
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    /** 리더 사용자 ID */
    @Column(name = "leader_user_id", nullable = false)
    private Integer leaderUserId;

    /** 조직 ID */
    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    /** 리더 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", insertable = false, updatable = false)
    private User leaderUser;

    /** 조직 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Organization organization;

    /** 팀에 속한 사용자 목록 */
    @ManyToMany
    @JoinTable(
            name = "userteam",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    /** 과제 목록 */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Task> tasks;
}
