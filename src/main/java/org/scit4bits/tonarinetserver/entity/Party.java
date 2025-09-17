package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 파티 엔티티
 */
@Entity
@Table(name = "party")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party {

    /** 파티 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 파티 이름 */
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    /** 리더 사용자 ID */
    @Column(name = "leader_user_id", nullable = false)
    private Integer leaderUserId;

    /** 리더 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", insertable = false, updatable = false)
    private User leaderUser;

    /** 파티 모집 완료 여부 */
    @Column(name = "is_finished", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFinished;

    /** 파티에 참여한 사용자 목록 */
    @ManyToMany
    @JoinTable(
            name = "userparty",
            joinColumns = @JoinColumn(name = "party_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;
}
