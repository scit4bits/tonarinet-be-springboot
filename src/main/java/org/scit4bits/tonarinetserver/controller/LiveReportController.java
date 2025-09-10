package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.LiveReportRequestDTO;
import org.scit4bits.tonarinetserver.dto.LiveReportResponseDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.LiveReportService;
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
@RequestMapping("/api/livereport")
@Tag(name = "Live Report", description = "Live report management API")
public class LiveReportController {

    private final LiveReportService liveReportService;

    @PostMapping
    @Operation(summary = "Create a new live report", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping
    @Operation(summary = "Get all live reports")
    public ResponseEntity<List<LiveReportResponseDTO>> getAllLiveReports() {
        try {
            List<LiveReportResponseDTO> reports = liveReportService.getAllLiveReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error fetching live reports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent live reports")
    public ResponseEntity<List<LiveReportResponseDTO>> getRecentLiveReports() {
        try {
            List<LiveReportResponseDTO> reports = liveReportService.getRecentLiveReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error fetching recent live reports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get live report by ID")
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

    @GetMapping("/near")
    @Operation(summary = "Get live reports near location")
    public ResponseEntity<List<LiveReportResponseDTO>> getLiveReportsNearLocation(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam(name="range", defaultValue = "0.1") Double range) {
        try {
            List<LiveReportResponseDTO> reports = liveReportService.getLiveReportsNearLocation(longitude, latitude, range);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error fetching live reports near location: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a live report", security = @SecurityRequirement(name = "bearerAuth"))
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

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a live report", security = @SecurityRequirement(name = "bearerAuth"))
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
