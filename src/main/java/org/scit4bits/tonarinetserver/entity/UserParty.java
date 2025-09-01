package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "userparty")
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

    @Column(name="entry_message")
    private String entryMessage;

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
