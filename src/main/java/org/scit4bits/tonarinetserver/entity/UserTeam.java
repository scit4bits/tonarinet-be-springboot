package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "userteam")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTeam {
    
    @EmbeddedId
    private UserTeamId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserTeamId {
        
        @Column(name = "user_id")
        private Integer userId;
        
        @Column(name = "team_id")
        private Integer teamId;
    }
}
