package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Region;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegionDTO {
    private Integer id;
    private String countryCode;
    private String category1;
    private String category2;
    private String category3;
    private String category4;
    private Double latitude;
    private Double longitude;
    private Integer radius;
    private String countryName;
    private Double distanceFromCenter; // Distance in degree units using Manhattan distance
    private Double averageReviewScore; // Average score of all town reviews for this region
    private Integer reviewsCount; // Total count of town reviews for this region

    public static RegionDTO fromEntity(Region region) {
        return RegionDTO.builder()
                .id(region.getId())
                .countryCode(region.getCountryCode())
                .category1(region.getCategory1())
                .category2(region.getCategory2())
                .category3(region.getCategory3())
                .category4(region.getCategory4())
                .latitude(region.getLatitude())
                .longitude(region.getLongitude())
                .radius(region.getRadius())
                .countryName(region.getCountry() != null ? region.getCountry().getName() : null)
                .averageReviewScore(0.0) // Will be set later by service
                .reviewsCount(0) // Will be set later by service
                .build();
    }

    public static RegionDTO fromEntityWithDistance(Region region, Double distance) {
        RegionDTO dto = fromEntity(region);
        dto.setDistanceFromCenter(distance);
        return dto;
    }
}
