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
        
        @Column(name = "role", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'user'")
        private String role;
    }
}
