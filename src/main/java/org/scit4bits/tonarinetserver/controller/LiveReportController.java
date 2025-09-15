package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.LiveReportRequestDTO;
import org.scit4bits.tonarinetserver.dto.LiveReportResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.LiveReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 실시간 제보 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/livereport")
@Tag(name = "Live Report", description = "실시간 제보 관리 API")
public class LiveReportController {

    private final LiveReportService liveReportService;

    /**
     * 새로운 실시간 제보를 생성합니다.
     * @param request 제보 생성 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 생성된 제보 정보
     */
    @PostMapping
    @Operation(summary = "새로운 실시간 제보 생성", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LiveReportResponseDTO> createLiveReport(
            @Valid @RequestBody LiveReportRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            LiveReportResponseDTO report = liveReportService.createLiveReport(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (Exception e) {
            log.error("Error creating live report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 모든 실시간 제보 목록을 조회합니다.
     * @return LiveReportResponseDTO 리스트
     */
    @GetMapping
    @Operation(summary = "모든 실시간 제보 조회")
    public ResponseEntity<List<LiveReportResponseDTO>> getAllLiveReports() {
        try {
            List<LiveReportResponseDTO> reports = liveReportService.getAllLiveReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error fetching live reports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 최신 실시간 제보 목록을 조회합니다.
     * @return LiveReportResponseDTO 리스트
     */
    @GetMapping("/recent")
    @Operation(summary = "최신 실시간 제보 조회")
    public ResponseEntity<List<LiveReportResponseDTO>> getRecentLiveReports() {
        try {
            List<LiveReportResponseDTO> reports = liveReportService.getRecentLiveReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error fetching recent live reports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ID로 특정 실시간 제보를 조회합니다.
     * @param id 조회할 제보 ID
     * @return LiveReportResponseDTO 형태의 제보 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 실시간 제보 조회")
    public ResponseEntity<LiveReportResponseDTO> getLiveReportById(@PathVariable("id") Integer id) {
        try {
            LiveReportResponseDTO report = liveReportService.getLiveReportById(id);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            log.error("Error fetching live report: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching live report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 위치 근처의 실시간 제보 목록을 조회합니다.
     * @param longitude 경도
     * @param latitude 위도
     * @param range 검색 반경
     * @return LiveReportResponseDTO 리스트
     */
    @GetMapping("/near")
    @Operation(summary = "위치 기반 실시간 제보 조회")
    public ResponseEntity<List<LiveReportResponseDTO>> getLiveReportsNearLocation(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam(name = "range", defaultValue = "0.1") Double range) {
        try {
            List<LiveReportResponseDTO> reports = liveReportService.getLiveReportsNearLocation(longitude, latitude, range);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error fetching live reports near location: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 실시간 제보를 삭제합니다.
     * @param id 삭제할 제보 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "실시간 제보 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteLiveReport(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            liveReportService.deleteLiveReport(id, user);
            return ResponseEntity.ok(new SimpleResponse("Live report deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the report creator") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting live report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 실시간 제보에 '좋아요'를 표시합니다.
     * @param id '좋아요'할 제보 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 업데이트된 제보 정보
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "실시간 제보 '좋아요'", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LiveReportResponseDTO> likeLiveReport(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            LiveReportResponseDTO report = liveReportService.likeLiveReport(id, user);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error liking live report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
