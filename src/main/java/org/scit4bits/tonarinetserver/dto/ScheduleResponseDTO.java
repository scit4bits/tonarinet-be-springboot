package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일정 조회 응답을 위한 데이터 전송 객체 (DTO)
 * 
 * <p>클라이언트에게 일정 정보를 전달할 때 사용됩니다.</p>
 * 
 * @author scit4bits
 * @version 1.0
 * @since 2025-09-18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponseDTO {
    /** 일정 ID */
    private Integer id;
    
    /** 일정 제목 */
    private String title;
    
    /** 일정 설명 */
    private String description;
    
    /** 일정 생성자 ID */
    private Integer createdById;
    
    /** 일정 생성자 이름 */
    private String createdByName;
    
    /** 일정 생성 날짜 및 시간 */
    private LocalDateTime createdAt;
    
    /** 일정 시작 날짜 및 시간 */
    private LocalDateTime fromWhen;
    
    /** 일정 종료 날짜 및 시간 */
    private LocalDateTime toWhen;
    
    /** 일정이 속한 조직 ID */
    private Integer orgId;

    /** 일정 유형 (예: "MEETING", "HOLIDAY" 등) */
    private String type;

    /** 하루종일 여부 */
    private Boolean allDay;

    /**
     * Schedule 엔티티를 ScheduleResponseDTO로 변환합니다.
     * 
     * @param schedule 변환할 Schedule 엔티티
     * @return 변환된 ScheduleResponseDTO 객체
     * @throws IllegalArgumentException schedule이 null인 경우
     */
    public static ScheduleResponseDTO fromEntity(Schedule schedule) {
        return ScheduleResponseDTO.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .createdById(schedule.getCreatedById())
                .createdByName(schedule.getCreatedBy() != null ? schedule.getCreatedBy().getNickname() : null)
                .createdAt(schedule.getCreatedAt())
                .fromWhen(schedule.getFromWhen())
                .toWhen(schedule.getToWhen())
                .orgId(schedule.getOrgId())
                .type(schedule.getType())
                .allDay(schedule.getAllDay())
                .build();
    }
}
