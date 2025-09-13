package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Country;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountryResponseDTO {
    private String countryCode;
    private String name;
    private String description;

    public static CountryResponseDTO fromEntity(Country country) {
        return CountryResponseDTO.builder()
                .countryCode(country.getCountryCode())
                .name(country.getName())
                .description(country.getDescription())
                .build();
    }
}
