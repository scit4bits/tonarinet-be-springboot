package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.RegionDTO;
import org.scit4bits.tonarinetserver.entity.Region;
import org.scit4bits.tonarinetserver.entity.TownReview;
import org.scit4bits.tonarinetserver.repository.RegionRepository;
import org.scit4bits.tonarinetserver.repository.TownReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 지역 정보 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;
    private final TownReviewRepository townReviewRepository;

    /**
     * 맨해튼 거리를 사용하여 중심 좌표로부터 특정 반경 내의 지역을 검색합니다.
     * 맨해튼 거리 = |lat1 - lat2| + |lon1 - lon2|
     *
     * @param latitude  중심 위도
     * @param longitude 중심 경도
     * @param radiusKm  반경 (도 단위의 단순화된 거리 측정)
     * @return 지정된 반경 내의 지역 리스트
     */
    public List<RegionDTO> searchRegionsWithinRadius(Double latitude, Double longitude, Double radiusKm) {
        log.info("맨해튼 거리를 사용하여 좌표: {}, {} 로부터 {} 도 단위 반경 내의 지역을 검색합니다.", latitude, longitude, radiusKm);

        // 입력 파라미터 유효성 검사
        if (latitude == null || longitude == null || radiusKm == null) {
            throw new IllegalArgumentException("위도, 경도, 반경은 필수 파라미터입니다.");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도는 -90에서 90 사이의 값이어야 합니다.");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도는 -180에서 180 사이의 값이어야 합니다.");
        }

        if (radiusKm <= 0) {
            throw new IllegalArgumentException("반경은 양수여야 합니다.");
        }

        List<Region> regions = regionRepository.findRegionsWithinRadius(latitude, longitude, radiusKm);
        List<RegionDTO> regionDTOs = new ArrayList<>();

        for (Region region : regions) {
            // DTO를 위해 맨해튼 거리 계산
            Double distance = Math.abs(region.getLatitude() - latitude) + Math.abs(region.getLongitude() - longitude);
            RegionDTO regionDTO = RegionDTO.fromEntityWithDistance(region, distance);
            enrichRegionWithReviewStats(regionDTO); // 리뷰 통계 정보 추가
            regionDTOs.add(regionDTO);
        }

        log.info("{} 도 단위 반경 내에서 {}개의 지역을 찾았습니다.", radiusKm, regionDTOs.size());
        return regionDTOs;
    }

    /**
     * 지역에 대한 리뷰 통계를 계산하고 RegionDTO에 설정합니다.
     * @param regionDTO 통계 정보를 추가할 RegionDTO
     */
    private void enrichRegionWithReviewStats(RegionDTO regionDTO) {
        List<TownReview> reviews = townReviewRepository.findByRegionIdOrderByLikeCountDesc(regionDTO.getId());

        regionDTO.setReviewsCount(reviews.size());

        if (reviews.isEmpty()) {
            regionDTO.setAverageReviewScore(0.0);
        } else {
            double totalScore = reviews.stream()
                    .mapToDouble(review -> (review.getTransportation() + review.getSafety() +
                            review.getInfra() + review.getPopulation() +
                            review.getEducation()) / 5.0)
                    .sum();
            double averageScore = totalScore / reviews.size();
            regionDTO.setAverageReviewScore(Math.round(averageScore * 10.0) / 10.0); // 소수점 첫째 자리까지 반올림
        }
    }

    /**
     * 특정 국가 내에서 맨해튼 거리를 사용하여 중심 좌표로부터 특정 반경 내의 지역을 검색합니다.
     *
     * @param latitude    중심 위도
     * @param longitude   중심 경도
     * @param radiusKm    반경 (도 단위의 단순화된 거리 측정)
     * @param countryCode 필터링할 국가 코드
     * @return 지정된 반경 및 국가 내의 지역 리스트
     */
    public List<RegionDTO> searchRegionsWithinRadiusAndCountry(Double latitude, Double longitude, Double radiusKm, String countryCode) {
        log.info("국가 {} 내에서 맨해튼 거리를 사용하여 좌표: {}, {} 로부터 {} 도 단위 반경 내의 지역을 검색합니다.", countryCode, latitude, longitude, radiusKm);

        if (latitude == null || longitude == null || radiusKm == null || countryCode == null) {
            throw new IllegalArgumentException("위도, 경도, 반경, 국가 코드는 필수 파라미터입니다.");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도는 -90에서 90 사이의 값이어야 합니다.");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도는 -180에서 180 사이의 값이어야 합니다.");
        }

        if (radiusKm <= 0) {
            throw new IllegalArgumentException("반경은 양수여야 합니다.");
        }

        List<Region> regions = regionRepository.findRegionsWithinRadiusAndCountry(latitude, longitude, radiusKm, countryCode);
        List<RegionDTO> regionDTOs = new ArrayList<>();

        for (Region region : regions) {
            Double distance = Math.abs(region.getLatitude() - latitude) + Math.abs(region.getLongitude() - longitude);
            RegionDTO regionDTO = RegionDTO.fromEntityWithDistance(region, distance);
            enrichRegionWithReviewStats(regionDTO);
            regionDTOs.add(regionDTO);
        }

        log.info("국가 {}의 {} 도 단위 반경 내에서 {}개의 지역을 찾았습니다.", countryCode, radiusKm, regionDTOs.size());
        return regionDTOs;
    }

    /**
     * ID로 특정 지역 정보를 조회합니다.
     * @param id 조회할 지역 ID
     * @return RegionDTO
     */
    public RegionDTO getRegionById(Integer id) {
        log.info("ID로 지역 정보 조회: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("지역 ID는 필수입니다.");
        }

        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("지역을 찾을 수 없습니다. ID: " + id));

        RegionDTO regionDTO = RegionDTO.fromEntity(region);
        enrichRegionWithReviewStats(regionDTO);
        return regionDTO;
    }

    /**
     * 모든 지역 정보를 조회합니다.
     * @return RegionDTO 리스트
     */
    public List<RegionDTO> getAllRegions() {
        log.info("모든 지역 정보 조회");

        List<Region> regions = regionRepository.findAll();
        return regions.stream()
                .map(region -> {
                    RegionDTO regionDTO = RegionDTO.fromEntity(region);
                    enrichRegionWithReviewStats(regionDTO);
                    return regionDTO;
                })
                .toList();
    }
}
