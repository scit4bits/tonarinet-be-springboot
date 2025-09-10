package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.RegionDTO;
import org.scit4bits.tonarinetserver.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RegionController provides READ-only endpoints for searching regions by geographic location.
 * Uses Manhattan distance for simple and fast distance calculations.
 * 
 * Main endpoint: GET /api/region/search
 * Required parameters: latitude, longitude, radius (in degree units)
 * Optional parameter: countryCode
 * 
 * Example usage:
 * - Search regions within 0.1 degree units of Tokyo: /api/region/search?latitude=35.6762&longitude=139.6503&radius=0.1
 * - Search regions in Japan only: /api/region/search?latitude=35.6762&longitude=139.6503&radius=0.1&countryCode=JP
 */
@Slf4j
@RestController
@RequestMapping("/api/region")
@RequiredArgsConstructor
public class RegionController {
    
    private final RegionService regionService;
    
    /**
     * Search regions within a radius from center coordinates using Manhattan distance
     * @param latitude Center latitude (required)
     * @param longitude Center longitude (required)
     * @param radius Radius in degree units (required) - uses Manhattan distance for simplicity
     * @param countryCode Optional country code to filter results
     * @return List of regions within the specified radius
     */
    @GetMapping("/search")
    public ResponseEntity<List<RegionDTO>> searchRegions(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("radius") Double radius,
            @RequestParam(value = "countryCode", required = false) String countryCode) {
        
        log.info("Search request - Latitude: {}, Longitude: {}, Radius: {} degree units, Country: {}", 
                latitude, longitude, radius, countryCode);
        
        try {
            List<RegionDTO> regions;
            
            if (countryCode != null && !countryCode.trim().isEmpty()) {
                regions = regionService.searchRegionsWithinRadiusAndCountry(latitude, longitude, radius, countryCode);
            } else {
                regions = regionService.searchRegionsWithinRadius(latitude, longitude, radius);
            }
            
            return ResponseEntity.ok(regions);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error searching regions", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get a specific region by ID
     * @param regionId Region ID
     * @return Region details
     */
    @GetMapping("/{regionId}")
    public ResponseEntity<RegionDTO> getRegion(@PathVariable("regionId") Integer regionId) {
        log.info("Get region request - ID: {}", regionId);
        
        try {
            RegionDTO region = regionService.getRegionById(regionId);
            return ResponseEntity.ok(region);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid region ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("Region not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting region", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all regions
     * @return List of all regions
     */
    @GetMapping
    public ResponseEntity<List<RegionDTO>> getAllRegions() {
        log.info("Get all regions request");
        
        try {
            List<RegionDTO> regions = regionService.getAllRegions();
            return ResponseEntity.ok(regions);
            
        } catch (Exception e) {
            log.error("Error getting all regions", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
