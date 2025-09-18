package org.scit4bits.tonarinetserver.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.ScheduleRequestDTO;
import org.scit4bits.tonarinetserver.dto.ScheduleResponseDTO;
import org.scit4bits.tonarinetserver.entity.Schedule;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 일정 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * <p>일정의 생성, 조회, 수정, 삭제 등의 비즈니스 로직을 담당합니다.</p>
 * 
 * @author scit4bits
 * @version 1.0
 * @since 2025-09-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    /**
     * 일정 ID로 특정 일정을 조회합니다.
     * 
     * @param schedId 조회할 일정 ID
     * @return 일정 상세 정보
     * @throws java.util.NoSuchElementException 해당 ID의 일정이 존재하지 않는 경우
     */
    public ScheduleResponseDTO getScheduleById(Integer schedId) {
        return scheduleRepository.findById(schedId)
                .map(ScheduleResponseDTO::fromEntity).get();
    }

    /**
     * 특정 조직의 일정 목록을 조회합니다.
     * 
     * @param orgId 조직 ID
     * @return 조직의 일정 목록
     */
    public List<ScheduleResponseDTO> getSchedulesByOrgId(Integer orgId) {
        return scheduleRepository.findByOrgId(orgId).stream()
                .map(ScheduleResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 조직 ID와 연도, 월로 일정 목록을 조회합니다.
     * 
     * <p>주어진 년월과 일정의 기간이 겹치는 모든 일정을 조회합니다.</p>
     * 
     * @param orgId 조직 ID
     * @param year 연도 (예: 2025)
     * @param month 월 (1-12)
     * @return 해당 월과 겹치는 일정 목록
     * 
     * @throws java.time.DateTimeException 잘못된 년도나 월이 제공된 경우
     * 
     * @apiNote 이 메서드는 Java의 YearMonth 클래스를 사용하여 정확한 날짜 계산을 수행합니다.
     * @implNote 
     * <ul>
     *   <li>윤년 계산이 자동으로 처리됩니다.</li>
     *   <li>월별 일수 차이가 자동으로 처리됩니다.</li>
     *   <li>일정 기간과 주어진 월이 겹치는 경우를 모두 포함합니다.</li>
     * </ul>
     */
    public List<ScheduleResponseDTO> getSchedulesByOrgIdAndYearMonth(Integer orgId, LocalDateTime start, LocalDateTime end) {
        log.info("조직 ID: {}, 시작일: {}, 종료일: {} 일정 조회", orgId, start, end);
        return scheduleRepository.findByOrgIdByRange(orgId, start, end).stream()
                .map(ScheduleResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 새로운 일정을 생성합니다.
     * 
     * @param user 일정을 생성하는 사용자
     * @param request 일정 생성 요청 정보
     * 
     * @throws IllegalArgumentException user 또는 request가 null인 경우
     * @throws org.springframework.dao.DataAccessException 데이터베이스 저장 시 오류가 발생한 경우
     * 
     * @apiNote 생성된 일정의 생성자는 현재 인증된 사용자로 설정됩니다.
     */
    public void createNewSchedule(User user, ScheduleRequestDTO request) {
        Schedule schedule = Schedule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .fromWhen(request.getFromWhen())
                .toWhen(request.getToWhen())
                .orgId(request.getOrgId())
                .createdById(user.getId())
                .build();
        scheduleRepository.save(schedule);
    }
}
