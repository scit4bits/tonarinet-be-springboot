package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.TownReviewRequestDTO;
import org.scit4bits.tonarinetserver.dto.TownReviewResponseDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.TownReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 동네 리뷰 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/townreview")
@Tag(name = "Town Review", description = "동네 리뷰 관리 API")
public class TownReviewController {

    private final TownReviewService townReviewService;

    /**
     * 새로운 동네 리뷰를 생성합니다.
     * @param request 리뷰 생성 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 생성된 리뷰 정보
     */
    @PostMapping
    @Operation(summary = "새로운 동네 리뷰 생성", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 모든 동네 리뷰 목록을 조회합니다.
     * @return TownReviewResponseDTO 리스트
     */
    @GetMapping
    @Operation(summary = "모든 동네 리뷰 조회")
    public ResponseEntity<List<TownReviewResponseDTO>> getAllTownReviews() {
        try {
            List<TownReviewResponseDTO> reviews = townReviewService.getAllTownReviews();
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching town reviews: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ID로 특정 동네 리뷰를 조회합니다.
     * @param id 조회할 리뷰 ID
     * @return TownReviewResponseDTO 형태의 리뷰 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 동네 리뷰 조회")
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

    /**
     * 특정 지역의 모든 동네 리뷰를 조회합니다.
     * @param regionId 지역 ID
     * @return TownReviewResponseDTO 리스트
     */
    @GetMapping("/region/{regionId}")
    @Operation(summary = "지역 ID로 동네 리뷰 조회")
    public ResponseEntity<List<TownReviewResponseDTO>> getTownReviewsByRegionId(@PathVariable("regionId") Integer regionId) {
        try {
            List<TownReviewResponseDTO> reviews = townReviewService.getTownReviewsByRegionId(regionId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching town reviews by region: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 국가의 모든 동네 리뷰를 조회합니다.
     * @param countryCode 국가 코드
     * @return TownReviewResponseDTO 리스트
     */
    @GetMapping("/country/{countryCode}")
    @Operation(summary = "국가 코드로 동네 리뷰 조회")
    public ResponseEntity<List<TownReviewResponseDTO>> getTownReviewsByCountryCode(@PathVariable("countryCode") String countryCode) {
        try {
            List<TownReviewResponseDTO> reviews = townReviewService.getTownReviewsByCountryCode(countryCode);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching town reviews by country: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 동네 리뷰를 수정합니다.
     * @param id 수정할 리뷰 ID
     * @param request 리뷰 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 리뷰 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "동네 리뷰 수정", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 특정 동네 리뷰를 삭제합니다.
     * @param id 삭제할 리뷰 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "동네 리뷰 삭제", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 특정 동네 리뷰에 '좋아요'를 표시합니다.
     * @param id '좋아요'할 리뷰 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 업데이트된 리뷰 정보
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "동네 리뷰 '좋아요'", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 동네 리뷰를 검색합니다.
     * @param searchBy 검색 기준 (all, title, content, author)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 TownReviewResponseDTO 리스트
     */
    @GetMapping("/search")
    @Operation(summary = "동네 리뷰 검색")
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
