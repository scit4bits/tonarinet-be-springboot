package org.scit4bits.tonarinetserver.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자-조직 역할 엔티티
 */
@Entity
@Table(name = "userrole")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    /** 복합 키 */
    @EmbeddedId
    private UserRoleId id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /** 조직 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orgId")
    @JoinColumn(name = "org_id")
    private Organization organization;

    /** 역할 */
    @Column(name = "role", length = 20)
    private String role;

    /** 승인 여부 */
    @Column(name = "is_granted", nullable = false, columnDefinition = "boolean default false")
    private Boolean isGranted;

    /** 입장 메시지 */
    @Column(name = "entry_message")
    private String entryMessage;

    /** 가입 신청 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 승인 일시 */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 사용자-조직 역할 ID 복합 키 클래스
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRoleId {

        /** 사용자 ID */
        @Column(name = "user_id")
        private Integer userId;

        /** 조직 ID */
        @Column(name = "org_id")
        private Integer orgId;
    }
}
