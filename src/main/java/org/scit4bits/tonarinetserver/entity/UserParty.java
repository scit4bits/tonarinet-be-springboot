package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자-파티 엔티티
 */
@Entity
@Table(name = "userparty")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserParty {

    /** 복합 키 */
    @EmbeddedId
    private UserPartyId id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /** 파티 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("partyId")
    @JoinColumn(name = "party_id")
    private Party party;

    /** 입장 메시지 */
    @Column(name = "entry_message")
    private String entryMessage;

    /** 승인 여부 */
    @Column(name = "is_granted")
    private Boolean isGranted;

    /**
     * 사용자-파티 ID 복합 키 클래스
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPartyId {

        /** 사용자 ID */
        @Column(name = "user_id")
        private Integer userId;

        /** 파티 ID */
        @Column(name = "party_id")
        private Integer partyId;
    }
}

