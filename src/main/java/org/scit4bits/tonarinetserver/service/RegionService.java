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

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;
    private final TownReviewRepository townReviewRepository;

    /**
     * Search regions within a radius from center coordinates using Manhattan distance
     * Manhattan distance = |lat1 - lat2| + |lon1 - lon2|
     *
     * @param latitude  Center latitude
     * @param longitude Center longitude
     * @param radiusKm  Radius in degree units (simplified distance measurement)
     * @return List of regions within the specified radius
     */
    public List<RegionDTO> searchRegionsWithinRadius(Double latitude, Double longitude, Double radiusKm) {
        log.info("Searching regions within {} degree units from coordinates: {}, {} using Manhattan distance", radiusKm, latitude, longitude);

        // Validate input parameters
        if (latitude == null || longitude == null || radiusKm == null) {
            throw new IllegalArgumentException("Latitude, longitude, and radius are required parameters");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }

        if (radiusKm <= 0) {
            throw new IllegalArgumentException("Radius must be a positive number");
        }

        List<Region> regions = regionRepository.findRegionsWithinRadius(latitude, longitude, radiusKm);
        List<RegionDTO> regionDTOs = new ArrayList<>();

        for (Region region : regions) {
            // Calculate Manhattan distance for the DTO
            Double distance = Math.abs(region.getLatitude() - latitude) + Math.abs(region.getLongitude() - longitude);
            RegionDTO regionDTO = RegionDTO.fromEntityWithDistance(region, distance);
            enrichRegionWithReviewStats(regionDTO);
            regionDTOs.add(regionDTO);
        }

        log.info("Found {} regions within {} degree units radius", regionDTOs.size(), radiusKm);
        return regionDTOs;
    }

    /**
     * Calculate review statistics for a region and set them in the RegionDTO
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
            regionDTO.setAverageReviewScore(Math.round(averageScore * 10.0) / 10.0); // Round to 1 decimal place
        }
    }

    /**
     * Search regions within a radius from center coordinates and specific country using Manhattan distance
     *
     * @param latitude    Center latitude
     * @param longitude   Center longitude
     * @param radiusKm    Radius in degree units (simplified distance measurement)
     * @param countryCode Country code to filter by
     * @return List of regions within the specified radius and country
     */
    public List<RegionDTO> searchRegionsWithinRadiusAndCountry(Double latitude, Double longitude, Double radiusKm, String countryCode) {
        log.info("Searching regions within {} degree units from coordinates: {}, {} in country: {} using Manhattan distance", radiusKm, latitude, longitude, countryCode);

        // Validate input parameters
        if (latitude == null || longitude == null || radiusKm == null || countryCode == null) {
            throw new IllegalArgumentException("Latitude, longitude, radius, and country code are required parameters");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }

        if (radiusKm <= 0) {
            throw new IllegalArgumentException("Radius must be a positive number");
        }

        List<Region> regions = regionRepository.findRegionsWithinRadiusAndCountry(latitude, longitude, radiusKm, countryCode);
        List<RegionDTO> regionDTOs = new ArrayList<>();

        for (Region region : regions) {
            // Calculate Manhattan distance for the DTO
            Double distance = Math.abs(region.getLatitude() - latitude) + Math.abs(region.getLongitude() - longitude);
            RegionDTO regionDTO = RegionDTO.fromEntityWithDistance(region, distance);
            enrichRegionWithReviewStats(regionDTO);
            regionDTOs.add(regionDTO);
        }

        log.info("Found {} regions within {} degree units radius in country {}", regionDTOs.size(), radiusKm, countryCode);
        return regionDTOs;
    }

    /**
     * Get a specific region by ID
     */
    public RegionDTO getRegionById(Integer id) {
        log.info("Getting region by ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Region ID is required");
        }

        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found with ID: " + id));

        RegionDTO regionDTO = RegionDTO.fromEntity(region);
        enrichRegionWithReviewStats(regionDTO);
        return regionDTO;
    }

    /**
     * Get all regions
     */
    public List<RegionDTO> getAllRegions() {
        log.info("Getting all regions");

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
