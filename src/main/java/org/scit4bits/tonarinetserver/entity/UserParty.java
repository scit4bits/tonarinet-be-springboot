package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UserParty")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserParty {
    
    @EmbeddedId
    private UserPartyId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("partyId")
    @JoinColumn(name = "party_id")
    private Party party;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPartyId {
        
        @Column(name = "user_id")
        private Integer userId;
        
        @Column(name = "party_id")
        private Integer partyId;
    }
}
