package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자-국가 엔티티
 */
@Entity
@Table(name = "usercountry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCountry {

    /** 복합 키 */
    @EmbeddedId
    private UserCountryId id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /** 국가 */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("countryCode")
    @JoinColumn(name = "country_code")
    private Country country;

    /** 역할 */
    @Column(name = "role", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'user'")
    private String role;

    /**
     * 사용자-국가 ID 복합 키 클래스
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserCountryId {

        /** 사용자 ID */
        @Column(name = "user_id")
        private Integer userId;

        /** 국가 코드 */
        @Column(name = "country_code", length = 5)
        private String countryCode;

    }
}

