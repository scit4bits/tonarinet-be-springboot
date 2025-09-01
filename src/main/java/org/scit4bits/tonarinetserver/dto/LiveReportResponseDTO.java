package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.LiveReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LiveReportResponseDTO {
    private Integer id;
    private String contents;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private Integer createdById;
    private String createdByName;
    private String longitude;
    private String latitude;

    public static LiveReportResponseDTO fromEntity(LiveReport liveReport) {
        return LiveReportResponseDTO.builder()
                .id(liveReport.getId())
                .contents(liveReport.getContents())
                .likeCount(liveReport.getLikeCount())
                .createdAt(liveReport.getCreatedAt())
                .createdById(liveReport.getCreatedById())
                .createdByName(liveReport.getCreatedBy() != null ? liveReport.getCreatedBy().getName() : null)
                .longitude(liveReport.getLongitude())
                .latitude(liveReport.getLatitude())
                .build();
    }
}
