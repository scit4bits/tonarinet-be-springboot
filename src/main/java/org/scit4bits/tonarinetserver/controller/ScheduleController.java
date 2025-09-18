package org.scit4bits.tonarinetserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.ScheduleRequestDTO;
import org.scit4bits.tonarinetserver.dto.ScheduleResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 일정 관련 REST API를 처리하는 컨트롤러
 * 
 * <p>일정의 생성, 조회 등의 기능을 제공합니다.</p>
 * 
 * @author scit4bits
 * @version 1.0
 * @since 2025-09-18
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {
    
    private final ScheduleService scheduleService;

    /**
     * 새로운 일정을 생성합니다.
     * 
     * @param user 인증된 사용자 정보
     * @param request 일정 생성 요청 데이터
     * @return 생성 결과를 포함한 응답
     * 
     * @apiNote POST /api/schedule
     * @implNote 인증된 사용자만 일정을 생성할 수 있습니다.
     */
    @PostMapping
    public ResponseEntity<SimpleResponse> postSchedule(@AuthenticationPrincipal User user, @RequestBody ScheduleRequestDTO request) {
        if(user == null) {
            return ResponseEntity.status(401).body(new SimpleResponse("error", "Unauthorized"));
        }
        try {
            scheduleService.createNewSchedule(user, request);
            return ResponseEntity.ok(new SimpleResponse("success", "Schedule created successfully"));
        } catch (Exception e) {
            log.error("Error in postSchedule: ", e);
            return ResponseEntity.status(500).body(new SimpleResponse("error", "Internal server error"));
        }
    }

    /**
     * 특정 일정의 상세 정보를 조회합니다.
     * 
     * @param user 인증된 사용자 정보
     * @param schedId 조회할 일정의 ID
     * @return 일정 상세 정보
     * 
     * @apiNote GET /api/schedule/{schedId}
     * @implNote 인증된 사용자만 일정 상세 정보를 조회할 수 있습니다.
     */
    @GetMapping("/{schedId}")
    public ResponseEntity<ScheduleResponseDTO> getScheduleDetail(@AuthenticationPrincipal User user, @PathVariable("schedId") Integer schedId) {
        if(user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            ScheduleResponseDTO response = scheduleService.getScheduleById(schedId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getMethodName: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 특정 조직의 일정 목록을 조회합니다.
     * 
     * @param user 인증된 사용자 정보
     * @param orgId 조회할 조직의 ID
     * @param yearmonth 선택적 연월 필터 (YYYY-MM 형식)
     * @return 조직의 일정 목록
     * 
     * @apiNote GET /api/schedule/org/{orgId}?yearmonth=YYYY-MM
     * @implNote 
     * <ul>
     *   <li>인증된 사용자만 일정 목록을 조회할 수 있습니다.</li>
     *   <li>yearmonth 파라미터가 제공되면 해당 연월의 일정만 필터링됩니다.</li>
     * </ul>
     */
    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<ScheduleResponseDTO>> getScheduleListByOrgId(
        @AuthenticationPrincipal User user, 
        @PathVariable("orgId") Integer orgId,
        @RequestParam(value = "start", required = false) LocalDateTime start,
        @RequestParam(value = "end", required = false) LocalDateTime end
        ) {
        if(user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            List<ScheduleResponseDTO> response = scheduleService.getSchedulesByOrgIdAndYearMonth(orgId, start, end).stream()
                    .map(ScheduleResponseDTO::fromEntity)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getScheduleListByOrgId: ", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    
    
}
