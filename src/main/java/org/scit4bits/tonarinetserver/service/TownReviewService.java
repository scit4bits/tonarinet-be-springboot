package org.scit4bits.tonarinetserver.service;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TownReviewRequestDTO;
import org.scit4bits.tonarinetserver.dto.TownReviewResponseDTO;
import org.scit4bits.tonarinetserver.entity.TownReview;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.TownReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TownReviewService {
    
    private final TownReviewRepository townReviewRepository;

    public TownReviewResponseDTO createTownReview(TownReviewRequestDTO request, User creator) {
        log.info("Creating town review for region: {} by user: {}", request.getRegionId(), creator.getId());
        
        TownReview townReview = TownReview.builder()
                .contents(request.getContents())
                .createdById(creator.getId())
                .transportation(request.getTransportation())
                .safety(request.getSafety())
                .infra(request.getInfra())
                .population(request.getPopulation())
                .education(request.getEducation())
                .regionId(request.getRegionId())
                .countryCode(request.getCountryCode())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .radius(request.getRadius())
                .likeCount(0)
                .build();
        
        TownReview savedReview = townReviewRepository.save(townReview);
        log.info("Town review created successfully with id: {}", savedReview.getId());
        return TownReviewResponseDTO.fromEntity(savedReview);
    }

    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getAllTownReviews() {
        log.info("Fetching all town reviews");
        return townReviewRepository.findAll().stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TownReviewResponseDTO getTownReviewById(Integer id) {
        log.info("Fetching town review with id: {}", id);
        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Town review not found with id: " + id));
        return TownReviewResponseDTO.fromEntity(townReview);
    }

    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getTownReviewsByRegionId(Integer regionId) {
        log.info("Fetching town reviews for region: {}", regionId);
        return townReviewRepository.findByRegionIdOrderByLikeCountDesc(regionId).stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getTownReviewsByCountryCode(String countryCode) {
        log.info("Fetching town reviews for country: {}", countryCode);
        return townReviewRepository.findByCountryCodeOrderByLikeCountDesc(countryCode).stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    public TownReviewResponseDTO updateTownReview(Integer id, TownReviewRequestDTO request, User user) {
        log.info("Updating town review with id: {} by user: {}", id, user.getId());
        
        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Town review not found with id: " + id));
        
        // Check if user is the creator or admin
        if (!townReview.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the review creator or admin can update the review");
        }
        
        townReview.setContents(request.getContents() != null && !request.getContents().trim().isEmpty() 
                              ? request.getContents() : townReview.getContents());
        townReview.setTransportation(request.getTransportation() != null 
                                   ? request.getTransportation() : townReview.getTransportation());
        townReview.setSafety(request.getSafety() != null 
                           ? request.getSafety() : townReview.getSafety());
        townReview.setInfra(request.getInfra() != null 
                          ? request.getInfra() : townReview.getInfra());
        townReview.setPopulation(request.getPopulation() != null 
                               ? request.getPopulation() : townReview.getPopulation());
        townReview.setEducation(request.getEducation() != null 
                              ? request.getEducation() : townReview.getEducation());
        townReview.setRegionId(request.getRegionId() != null 
                             ? request.getRegionId() : townReview.getRegionId());
        townReview.setCountryCode(request.getCountryCode() != null && !request.getCountryCode().trim().isEmpty() 
                                ? request.getCountryCode() : townReview.getCountryCode());
        townReview.setLongitude(request.getLongitude() != null && !request.getLongitude().trim().isEmpty() 
                              ? request.getLongitude() : townReview.getLongitude());
        townReview.setLatitude(request.getLatitude() != null && !request.getLatitude().trim().isEmpty() 
                             ? request.getLatitude() : townReview.getLatitude());
        townReview.setRadius(request.getRadius() != null 
                           ? request.getRadius() : townReview.getRadius());
        
        TownReview savedReview = townReviewRepository.save(townReview);
        log.info("Town review updated successfully");
        return TownReviewResponseDTO.fromEntity(savedReview);
    }

    public void deleteTownReview(Integer id, User user) {
        log.info("Deleting town review with id: {} by user: {}", id, user.getId());
        
        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Town review not found with id: " + id));
        
        // Check if user is the creator or admin
        if (!townReview.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the review creator or admin can delete the review");
        }
        
        townReviewRepository.deleteById(id);
        log.info("Town review deleted successfully");
    }

    public TownReviewResponseDTO likeTownReview(Integer id, User user) {
        log.info("User {} liking town review {}", user.getId(), id);
        
        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Town review not found with id: " + id));
        
        // In a real implementation, you would check if the user has already liked this review
        // For now, just increment the like count
        townReview.setLikeCount(townReview.getLikeCount() + 1);
        
        TownReview savedReview = townReviewRepository.save(townReview);
        log.info("Town review liked successfully");
        return TownReviewResponseDTO.fromEntity(savedReview);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TownReviewResponseDTO> searchTownReviews(String searchBy, String search, Integer page, 
                                                                 Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching town reviews with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}", 
                searchBy, search, page, pageSize, sortBy, sortDirection);
        
        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";
        
        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // sortBy 필드명 매핑
        String entityFieldName;
        switch (sortByField.toLowerCase()) {
            case "id":
                entityFieldName = "id";
                break;
            case "created":
                entityFieldName = "createdAt";
                break;
            case "likes":
                entityFieldName = "likeCount";
                break;
            case "region":
                entityFieldName = "regionId";
                break;
            case "country":
                entityFieldName = "countryCode";
                break;
            default:
                entityFieldName = "id";
                break;
        }
        
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<TownReview> reviewPage;
        
        if (search == null || search.trim().isEmpty()) {
            reviewPage = townReviewRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    reviewPage = townReviewRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        reviewPage = townReviewRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                case "contents":
                    reviewPage = townReviewRepository.findByContentsContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "creator":
                    try {
                        Integer creatorId = Integer.parseInt(search.trim());
                        reviewPage = townReviewRepository.findByCreatedById(creatorId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid creator ID format for search: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                case "region":
                    try {
                        Integer regionId = Integer.parseInt(search.trim());
                        reviewPage = townReviewRepository.findByRegionId(regionId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid region ID format for search: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                case "country":
                    reviewPage = townReviewRepository.findByCountryCode(search.trim(), pageable);
                    break;
                case "rating":
                    try {
                        double rating = Double.parseDouble(search.trim());
                        // Search for reviews with average rating within 0.5 range
                        reviewPage = townReviewRepository.findByAverageRatingBetween(
                            rating - 0.5, rating + 0.5, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid rating format for search: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                case "longitude":
                    reviewPage = townReviewRepository.findByLongitudeContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "latitude":
                    reviewPage = townReviewRepository.findByLatitudeContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "radius":
                    try {
                        Integer radiusValue = Integer.parseInt(search.trim());
                        reviewPage = townReviewRepository.findByRadius(radiusValue, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid radius format for search: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    reviewPage = townReviewRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }
        
        List<TownReviewResponseDTO> result = reviewPage.getContent().stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
        
        log.info("Found {} town reviews out of {} total", result.size(), reviewPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, reviewPage.getTotalElements(), reviewPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getTownReviewsByLocation(String longitude, String latitude, Integer radius) {
        log.info("Fetching town reviews by location: longitude={}, latitude={}, radius={}", longitude, latitude, radius);
        return townReviewRepository.findByLongitudeAndLatitudeAndRadius(longitude, latitude, radius).stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getTownReviewsByMaxRadius(Integer maxRadius) {
        log.info("Fetching town reviews with radius <= {}", maxRadius);
        return townReviewRepository.findByRadiusLessThanEqual(maxRadius).stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getTownReviewsByMinRadius(Integer minRadius) {
        log.info("Fetching town reviews with radius >= {}", minRadius);
        return townReviewRepository.findByRadiusGreaterThanEqual(minRadius).stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }
}
