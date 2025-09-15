package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 지역(Region) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {

    /**
     * 맨해튼 거리를 사용하여 중심점으로부터 특정 반경 내의 지역을 찾습니다.
     * 맨해튼 거리 = |lat1 - lat2| + |lon1 - lon2|
     *
     * @param latitude  중심 위도
     * @param longitude 중심 경도
     * @param radiusKm  킬로미터 단위의 반경 (근사치)
     * @return 지정된 반경 내의 지역 리스트
     */
    @Query("SELECT r FROM Region r " +
            "WHERE r.latitude IS NOT NULL " +
            "AND r.longitude IS NOT NULL " +
            "AND (ABS(r.latitude - :latitude) + ABS(r.longitude - :longitude)) <= :radiusKm " +
            "ORDER BY (ABS(r.latitude - :latitude) + ABS(r.longitude - :longitude))")
    List<Region> findRegionsWithinRadius(@Param("latitude") Double latitude,
                                         @Param("longitude") Double longitude,
                                         @Param("radiusKm") Double radiusKm);

    /**
     * 특정 국가 내에서 맨해튼 거리를 사용하여 중심점으로부터 특정 반경 내의 지역을 찾습니다.
     * @param latitude 중심 위도
     * @param longitude 중심 경도
     * @param radiusKm 킬로미터 단위의 반경 (근사치)
     * @param countryCode 국가 코드
     * @return 지정된 반경 내의 지역 리스트
     */
    @Query("SELECT r FROM Region r " +
            "WHERE r.latitude IS NOT NULL " +
            "AND r.longitude IS NOT NULL " +
            "AND r.countryCode = :countryCode " +
            "AND (ABS(r.latitude - :latitude) + ABS(r.longitude - :longitude)) <= :radiusKm " +
            "ORDER BY (ABS(r.latitude - :latitude) + ABS(r.longitude - :longitude))")
    List<Region> findRegionsWithinRadiusAndCountry(@Param("latitude") Double latitude,
                                                   @Param("longitude") Double longitude,
                                                   @Param("radiusKm") Double radiusKm,
                                                   @Param("countryCode") String countryCode);
}
