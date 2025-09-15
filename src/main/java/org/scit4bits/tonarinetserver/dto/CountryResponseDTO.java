package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Country;

/**
 * 국가 정보를 전달하기 위한 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountryResponseDTO {
    private String countryCode;
    private String name;
    private String description;

    /**
     * Country 엔티티를 CountryResponseDTO로 변환합니다.
     * @param country 변환할 Country 엔티티
     * @return 변환된 CountryResponseDTO 객체
     */
    public static CountryResponseDTO fromEntity(Country country) {
        return CountryResponseDTO.builder()
                .countryCode(country.getCountryCode())
                .name(country.getName())
                .description(country.getDescription())
                .build();
    }
}
