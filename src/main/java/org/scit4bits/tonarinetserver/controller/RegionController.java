package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.RegionDTO;
import org.scit4bits.tonarinetserver.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 지역 정보를 조회하는 읽기 전용 API를 제공합니다.
 * 맨해튼 거리를 사용하여 간단하고 빠른 거리 계산을 수행합니다.
 * <p>
 * 메인 엔드포인트: GET /api/region/search
 * 필수 파라미터: latitude, longitude, radius (도 단위)
 * 선택 파라미터: countryCode
 * <p>
 * 사용 예시:
 * - 도쿄 중심 0.1도 반경 내 지역 검색: /api/region/search?latitude=35.6762&amp;longitude=139.6503&amp;radius=0.1
 * - 일본 내 지역만 검색: /api/region/search?latitude=35.6762&amp;longitude=139.6503&amp;radius=0.1&amp;countryCode=JP
 */
@Slf4j
@RestController
@RequestMapping("/api/region")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    /**
     * 맨해튼 거리를 사용하여 중심 좌표로부터 특정 반경 내의 지역을 검색합니다.
     *
     * @param latitude    중심 위도 (필수)
     * @param longitude   중심 경도 (필수)
     * @param radius      반경 (도 단위, 필수) - 단순화를 위해 맨해튼 거리 사용
     * @param countryCode 결과를 필터링할 국가 코드 (선택)
     * @return 지정된 반경 내의 지역 리스트
     */
    @GetMapping("/search")
    public ResponseEntity<List<RegionDTO>> searchRegions(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("radius") Double radius,
            @RequestParam(value = "countryCode", required = false) String countryCode) {

        log.info("지역 검색 요청 - 위도: {}, 경도: {}, 반경: {}도, 국가: {}",
                latitude, longitude, radius, countryCode);

        try {
            List<RegionDTO> regions;

            // 국가 코드가 제공된 경우, 국가별로 필터링하여 검색합니다.
            if (countryCode != null && !countryCode.trim().isEmpty()) {
                regions = regionService.searchRegionsWithinRadiusAndCountry(latitude, longitude, radius, countryCode);
            } else {
                regions = regionService.searchRegionsWithinRadius(latitude, longitude, radius);
            }

            return ResponseEntity.ok(regions);

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 검색 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("지역 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ID로 특정 지역 정보를 조회합니다.
     *
     * @param regionId 지역 ID
     * @return 지역 상세 정보
     */
    @GetMapping("/{regionId}")
    public ResponseEntity<RegionDTO> getRegion(@PathVariable("regionId") Integer regionId) {
        log.info("지역 정보 조회 요청 - ID: {}", regionId);

        try {
            RegionDTO region = regionService.getRegionById(regionId);
            return ResponseEntity.ok(region);

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 지역 ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("지역을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("지역 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 모든 지역 정보를 조회합니다.
     *
     * @return 모든 지역 리스트
     */
    @GetMapping
    public ResponseEntity<List<RegionDTO>> getAllRegions() {
        log.info("모든 지역 정보 조회 요청");

        try {
            List<RegionDTO> regions = regionService.getAllRegions();
            return ResponseEntity.ok(regions);

        } catch (Exception e) {
            log.error("모든 지역 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
