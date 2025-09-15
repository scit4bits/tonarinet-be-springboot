package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Region;

/**
 * 지역 정보를 전달하기 위한 DTO
 */
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
    private Double distanceFromCenter; // 맨해튼 거리를 사용한 중심으로부터의 거리 (도 단위)
    private Double averageReviewScore; // 이 지역에 대한 모든 동네 리뷰의 평균 점수
    private Integer reviewsCount; // 이 지역에 대한 동네 리뷰의 총 개수

    /**
     * Region 엔티티를 RegionDTO로 변환합니다.
     * @param region 변환할 Region 엔티티
     * @return 변환된 RegionDTO 객체
     */
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
                .averageReviewScore(0.0) // 서비스에서 나중에 설정될 값
                .reviewsCount(0) // 서비스에서 나중에 설정될 값
                .build();
    }

    /**
     * Region 엔티티와 거리를 포함하여 RegionDTO로 변환합니다.
     * @param region 변환할 Region 엔티티
     * @param distance 중심으로부터의 거리
     * @return 변환된 RegionDTO 객체
     */
    public static RegionDTO fromEntityWithDistance(Region region, Double distance) {
        RegionDTO dto = fromEntity(region);
        dto.setDistanceFromCenter(distance);
        return dto;
    }
}
