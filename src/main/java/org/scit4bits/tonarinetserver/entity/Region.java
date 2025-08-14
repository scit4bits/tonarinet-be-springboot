package org.scit4bits.tonarinetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Region")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "country_code", length = 5, nullable = false)
    private String countryCode;
    
    @Column(name = "category1", length = 20)
    private String category1;
    
    @Column(name = "category2", length = 20)
    private String category2;
    
    @Column(name = "category3", length = 20)
    private String category3;
    
    @Column(name = "category4", length = 20)
    private String category4;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", insertable = false, updatable = false)
    private Country country;
}
