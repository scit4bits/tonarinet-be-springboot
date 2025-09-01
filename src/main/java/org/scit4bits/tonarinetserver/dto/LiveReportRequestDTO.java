package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LiveReportRequestDTO {
    
    @NotBlank(message = "Contents is required")
    private String contents;
    
    @NotBlank(message = "Longitude is required")
    private String longitude;
    
    @NotBlank(message = "Latitude is required")
    private String latitude;
}
