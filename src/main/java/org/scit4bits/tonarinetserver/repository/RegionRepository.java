package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {

    /**
     * Find regions within a certain radius from a center point using Manhattan distance
     * Manhattan distance = |lat1 - lat2| + |lon1 - lon2|
     *
     * @param latitude  Center latitude
     * @param longitude Center longitude
     * @param radiusKm  Radius in kilometers (approximated using degree differences)
     * @return List of regions within the specified radius
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
     * Find regions within a certain radius and specific country using Manhattan distance
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
