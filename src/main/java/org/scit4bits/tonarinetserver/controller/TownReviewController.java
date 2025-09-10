package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.TownReviewRequestDTO;
import org.scit4bits.tonarinetserver.dto.TownReviewResponseDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.TownReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/townreview")
@Tag(name = "Town Review", description = "Town review management API")
public class TownReviewController {

    private final TownReviewService townReviewService;

    @PostMapping
    @Operation(summary = "Create a new town review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TownReviewResponseDTO> createTownReview(
            @Valid @RequestBody TownReviewRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            TownReviewResponseDTO review = townReviewService.createTownReview(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (Exception e) {
            log.error("Error creating town review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all town reviews")
    public ResponseEntity<List<TownReviewResponseDTO>> getAllTownReviews() {
        try {
            List<TownReviewResponseDTO> reviews = townReviewService.getAllTownReviews();
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching town reviews: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get town review by ID")
    public ResponseEntity<TownReviewResponseDTO> getTownReviewById(@PathVariable("id") Integer id) {
        try {
            TownReviewResponseDTO review = townReviewService.getTownReviewById(id);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Error fetching town review: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching town review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Get town reviews by region ID")
    public ResponseEntity<List<TownReviewResponseDTO>> getTownReviewsByRegionId(@PathVariable("regionId") Integer regionId) {
        try {
            List<TownReviewResponseDTO> reviews = townReviewService.getTownReviewsByRegionId(regionId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching town reviews by region: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/country/{countryCode}")
    @Operation(summary = "Get town reviews by country code")
    public ResponseEntity<List<TownReviewResponseDTO>> getTownReviewsByCountryCode(@PathVariable("countryCode") String countryCode) {
        try {
            List<TownReviewResponseDTO> reviews = townReviewService.getTownReviewsByCountryCode(countryCode);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching town reviews by country: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a town review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TownReviewResponseDTO> updateTownReview(
            @PathVariable("id") Integer id,
            @Valid @RequestBody TownReviewRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            TownReviewResponseDTO review = townReviewService.updateTownReview(id, request, user);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the review creator") || 
                      e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating town review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a town review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteTownReview(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            townReviewService.deleteTownReview(id, user);
            return ResponseEntity.ok(new SimpleResponse("Town review deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the review creator") || 
                      e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting town review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a town review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TownReviewResponseDTO> likeTownReview(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            TownReviewResponseDTO review = townReviewService.likeTownReview(id, user);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error liking town review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search town reviews")
    public ResponseEntity<PagedResponse<TownReviewResponseDTO>> searchTownReviews(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        try {
            PagedResponse<TownReviewResponseDTO> reviews = townReviewService.searchTownReviews(
                searchBy, search, page, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error searching town reviews: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
