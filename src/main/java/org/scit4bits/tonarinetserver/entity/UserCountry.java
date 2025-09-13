package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usercountry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCountry {

    @EmbeddedId
    private UserCountryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("countryCode")
    @JoinColumn(name = "country_code")
    private Country country;

    @Column(name = "role", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'user'")
    private String role;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserCountryId {

        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "country_code", length = 5)
        private String countryCode;

    }
}
