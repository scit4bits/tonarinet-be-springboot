package org.scit4bits.tonarinetserver.service;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.LiveReportRequestDTO;
import org.scit4bits.tonarinetserver.dto.LiveReportResponseDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.LiveReport;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.LiveReportRepository;
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
public class LiveReportService {
    
    private final LiveReportRepository liveReportRepository;

    public LiveReportResponseDTO createLiveReport(LiveReportRequestDTO request, User creator) {
        log.info("Creating live report at location ({}, {}) by user: {}", 
                request.getLongitude(), request.getLatitude(), creator.getId());
        
        LiveReport liveReport = LiveReport.builder()
                .contents(request.getContents())
                .createdById(creator.getId())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .likeCount(0)
                .build();
        
        LiveReport savedReport = liveReportRepository.save(liveReport);
        log.info("Live report created successfully with id: {}", savedReport.getId());
        return LiveReportResponseDTO.fromEntity(savedReport);
    }

    @Transactional(readOnly = true)
    public List<LiveReportResponseDTO> getAllLiveReports() {
        log.info("Fetching all live reports");
        return liveReportRepository.findAll().stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LiveReportResponseDTO getLiveReportById(Integer id) {
        log.info("Fetching live report with id: {}", id);
        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live report not found with id: " + id));
        return LiveReportResponseDTO.fromEntity(liveReport);
    }

    @Transactional(readOnly = true)
    public List<LiveReportResponseDTO> getRecentLiveReports() {
        log.info("Fetching recent live reports");
        return liveReportRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LiveReportResponseDTO> getLiveReportsNearLocation(Double longitude, Double latitude, Double range) {
        log.info("Fetching live reports near location ({}, {}) within range: {}", longitude, latitude, range);
        return liveReportRepository.findByLocationRange(longitude, latitude, range).stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();
    }

    public LiveReportResponseDTO updateLiveReport(Integer id, LiveReportRequestDTO request, User user) {
        log.info("Updating live report with id: {} by user: {}", id, user.getId());
        
        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live report not found with id: " + id));
        
        // Check if user is the creator or admin
        if (!liveReport.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the report creator or admin can update the report");
        }
        
        liveReport.setContents(request.getContents() != null && !request.getContents().trim().isEmpty() 
                              ? request.getContents() : liveReport.getContents());
        liveReport.setLongitude(request.getLongitude() != null 
                               ? request.getLongitude() : liveReport.getLongitude());
        liveReport.setLatitude(request.getLatitude() != null 
                              ? request.getLatitude() : liveReport.getLatitude());
        
        LiveReport savedReport = liveReportRepository.save(liveReport);
        log.info("Live report updated successfully");
        return LiveReportResponseDTO.fromEntity(savedReport);
    }

    public void deleteLiveReport(Integer id, User user) {
        log.info("Deleting live report with id: {} by user: {}", id, user.getId());
        
        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live report not found with id: " + id));
        
        // Check if user is the creator or admin
        if (!liveReport.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the report creator or admin can delete the report");
        }
        
        liveReportRepository.deleteById(id);
        log.info("Live report deleted successfully");
    }

    public LiveReportResponseDTO likeLiveReport(Integer id, User user) {
        log.info("User {} liking live report {}", user.getId(), id);
        
        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live report not found with id: " + id));
        
        // In a real implementation, you would check if the user has already liked this report
        // For now, just increment the like count
        liveReport.setLikeCount(liveReport.getLikeCount() + 1);
        
        LiveReport savedReport = liveReportRepository.save(liveReport);
        log.info("Live report liked successfully");
        return LiveReportResponseDTO.fromEntity(savedReport);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LiveReportResponseDTO> searchLiveReports(String searchBy, String search, Integer page, 
                                                                 Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching live reports with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}", 
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
            default:
                entityFieldName = "id";
                break;
        }
        
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<LiveReport> reportPage;
        
        if (search == null || search.trim().isEmpty()) {
            reportPage = liveReportRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    reportPage = liveReportRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        reportPage = liveReportRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        reportPage = Page.empty(pageable);
                    }
                    break;
                case "contents":
                    reportPage = liveReportRepository.findByContentsContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "creator":
                    try {
                        Integer creatorId = Integer.parseInt(search.trim());
                        reportPage = liveReportRepository.findByCreatedById(creatorId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid creator ID format for search: {}", search);
                        reportPage = Page.empty(pageable);
                    }
                    break;
                case "likes":
                    try {
                        Integer likeCount = Integer.parseInt(search.trim());
                        reportPage = liveReportRepository.findByLikeCountBetween(
                            likeCount - 5, likeCount + 5, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid like count format for search: {}", search);
                        reportPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    reportPage = liveReportRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }
        
        List<LiveReportResponseDTO> result = reportPage.getContent().stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();
        
        log.info("Found {} live reports out of {} total", result.size(), reportPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, reportPage.getTotalElements(), reportPage.getTotalPages());
    }
}
