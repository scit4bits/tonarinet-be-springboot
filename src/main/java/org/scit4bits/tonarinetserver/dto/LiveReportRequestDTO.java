package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LiveReportRequestDTO {

    @NotNull(message = "Contents is required")
    private String contents;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Latitude is required")
    private Double latitude;
}
