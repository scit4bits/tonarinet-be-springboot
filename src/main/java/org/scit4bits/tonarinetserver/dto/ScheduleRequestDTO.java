package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일정 생성 및 수정 요청을 위한 데이터 전송 객체 (DTO)
 * 
 * <p>클라이언트로부터 일정 관련 데이터를 수신할 때 사용됩니다.</p>
 * 
 * @author scit4bits
 * @version 1.0
 * @since 2025-09-18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequestDTO {
    
    /** 일정 제목 */
    private String title;
    
    /** 일정 설명 */
    private String description;
    
    /** 일정 시작 날짜 및 시간 */
    private LocalDateTime fromWhen;
    
    /** 일정 종료 날짜 및 시간 */
    private LocalDateTime toWhen;
    
    /** 일정이 속한 조직 ID */
    private Integer orgId;

    /** 일정 유형 (예: "MEETING", "HOLIDAY" 등) */
    private String type;
}
