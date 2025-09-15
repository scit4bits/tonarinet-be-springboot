package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.LiveReport;

import java.time.LocalDateTime;

/**
 * 실시간 제보 응답을 위한 DTO
 */
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
    private Double longitude;
    private Double latitude;

    /**
     * LiveReport 엔티티를 LiveReportResponseDTO로 변환합니다.
     * @param liveReport 변환할 LiveReport 엔티티
     * @return 변환된 LiveReportResponseDTO 객체
     */
    public static LiveReportResponseDTO fromEntity(LiveReport liveReport) {
        return LiveReportResponseDTO.builder()
                .id(liveReport.getId())
                .contents(liveReport.getContents())
                .likeCount(liveReport.getLikeCount())
                .createdAt(liveReport.getCreatedAt())
                .createdById(liveReport.getCreatedById())
                .createdByName(liveReport.getCreatedBy() != null ? liveReport.getCreatedBy().getNickname() : null)
                .longitude(liveReport.getLongitude())
                .latitude(liveReport.getLatitude())
                .build();
    }
}
