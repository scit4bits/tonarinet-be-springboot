package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자-팀 엔티티
 */
@Entity
@Table(name = "userteam")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTeam {

    /** 복합 키 */
    @EmbeddedId
    private UserTeamId id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /** 팀 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    /**
     * 사용자-팀 ID 복합 키 클래스
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserTeamId {

        /** 사용자 ID */
        @Column(name = "user_id")
        private Integer userId;

        /** 팀 ID */
        @Column(name = "team_id")
        private Integer teamId;
    }
}

