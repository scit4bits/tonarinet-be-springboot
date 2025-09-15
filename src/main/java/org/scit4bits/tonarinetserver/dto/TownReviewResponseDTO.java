package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.TownReview;

import java.time.LocalDateTime;

/**
 * 동네 리뷰 응답을 위한 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TownReviewResponseDTO {
    private Integer id;
    private String contents;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private Integer transportation;
    private Integer safety;
    private Integer infra;
    private Integer population;
    private Integer education;
    private Integer regionId;
    private String regionName;
    private String countryCode;
    private String countryName;
    private Integer likeCount;
    private String longitude;
    private String latitude;
    private Integer radius;
    private Double averageRating;

    /**
     * TownReview 엔티티를 TownReviewResponseDTO로 변환합니다.
     * @param townReview 변환할 TownReview 엔티티
     * @return 변환된 TownReviewResponseDTO 객체
     */
    public static TownReviewResponseDTO fromEntity(TownReview townReview) {
        double avgRating = (townReview.getTransportation() + townReview.getSafety() +
                townReview.getInfra() + townReview.getPopulation() +
                townReview.getEducation()) / 5.0;

        return TownReviewResponseDTO.builder()
                .id(townReview.getId())
                .contents(townReview.getContents())
                .createdById(townReview.getCreatedById())
                .createdByName(townReview.getCreatedBy() != null ? townReview.getCreatedBy().getNickname() : null)
                .createdAt(townReview.getCreatedAt())
                .transportation(townReview.getTransportation())
                .safety(townReview.getSafety())
                .infra(townReview.getInfra())
                .population(townReview.getPopulation())
                .education(townReview.getEducation())
                .regionId(townReview.getRegionId())
                .regionName(townReview.getRegion() != null ?
                        (townReview.getRegion().getCategory1() +
                                (townReview.getRegion().getCategory2() != null ? " " + townReview.getRegion().getCategory2() : "") +
                                (townReview.getRegion().getCategory3() != null ? " " + townReview.getRegion().getCategory3() : "") +
                                (townReview.getRegion().getCategory4() != null ? " " + townReview.getRegion().getCategory4() : "")) : null)
                .countryCode(townReview.getCountryCode())
                .countryName(townReview.getCountry() != null ? townReview.getCountry().getName() : null)
                .likeCount(townReview.getLikeCount())
                .averageRating(Math.round(avgRating * 10.0) / 10.0) // 소수점 첫째 자리에서 반올림
                .build();
    }
}
