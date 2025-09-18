package org.scit4bits.tonarinetserver.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.scit4bits.tonarinetserver.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 일정(Schedule) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 * 
 * <p>일정 조회, 생성, 수정, 삭제 등의 데이터베이스 작업을 제공합니다.</p>
 * 
 * @author scit4bits
 * @version 1.0
 * @since 2025-09-18
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    
    /**
     * 특정 조직의 모든 일정을 조회합니다.
     * 
     * @param orgId 조직 ID
     * @return 해당 조직의 일정 목록
     */
    List<Schedule> findByOrgId(Integer orgId);

    /**
     * 조직 ID와 년월 조건에 맞는 일정 목록을 조회합니다.
     * 주어진 년월의 범위와 일정의 시작/종료 시간이 겹치는 일정을 찾습니다.
     * 
     * @param orgId 조직 ID
     * @param monthStart 해당 월의 시작일 (예: 2025-09-01 00:00:00)
     * @param monthEnd 해당 월의 마지막일 (예: 2025-09-30 23:59:59)
     * @return 조건에 맞는 일정 목록
     */
    @Query("SELECT s FROM Schedule s WHERE s.orgId = :orgId " +
           "AND ((s.fromWhen <= :end AND s.toWhen >= :start) " +
           "OR (s.fromWhen IS NULL OR s.toWhen IS NULL))")
    List<Schedule> findByOrgIdByRange(
        @Param("orgId") Integer orgId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );


}
