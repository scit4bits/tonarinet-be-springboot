package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 동네 리뷰 생성 및 수정을 위한 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TownReviewRequestDTO {

    @NotBlank(message = "Contents is required")
    private String contents;

    @NotNull(message = "Transportation rating is required")
    @Min(value = 0, message = "Transportation rating must be between 0 and 5")
    @Max(value = 5, message = "Transportation rating must be between 0 and 5")
    private Integer transportation;

    @NotNull(message = "Safety rating is required")
    @Min(value = 0, message = "Safety rating must be between 0 and 5")
    @Max(value = 5, message = "Safety rating must be between 0 and 5")
    private Integer safety;

    @NotNull(message = "Infrastructure rating is required")
    @Min(value = 0, message = "Infrastructure rating must be between 0 and 5")
    @Max(value = 5, message = "Infrastructure rating must be between 0 and 5")
    private Integer infra;

    @NotNull(message = "Population rating is required")
    @Min(value = 0, message = "Population rating must be between 0 and 5")
    @Max(value = 5, message = "Population rating must be between 0 and 5")
    private Integer population;

    @NotNull(message = "Education rating is required")
    @Min(value = 0, message = "Education rating must be between 0 and 5")
    @Max(value = 5, message = "Education rating must be between 0 and 5")
    private Integer education;

    @NotNull(message = "Region ID is required")
    private Integer regionId;
}
