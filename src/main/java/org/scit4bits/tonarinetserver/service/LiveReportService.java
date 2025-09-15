package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;

/**
 * 실시간 제보 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LiveReportService {

    private final LiveReportRepository liveReportRepository;

    /**
     * 새로운 실시간 제보를 생성합니다.
     * @param request 제보 생성 요청 정보
     * @param creator 작성자 정보
     * @return 생성된 제보 정보
     */
    public LiveReportResponseDTO createLiveReport(LiveReportRequestDTO request, User creator) {
        log.info("실시간 제보 생성 - 위치: ({}, {}), 작성자: {}",
                request.getLongitude(), request.getLatitude(), creator.getId());

        LiveReport liveReport = LiveReport.builder()
                .contents(request.getContents())
                .createdById(creator.getId())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .likeCount(0)
                .build();

        LiveReport savedReport = liveReportRepository.save(liveReport);
        log.info("실시간 제보 생성 완료, ID: {}", savedReport.getId());
        return LiveReportResponseDTO.fromEntity(savedReport);
    }

    /**
     * 모든 실시간 제보 목록을 조회합니다.
     * @return LiveReportResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<LiveReportResponseDTO> getAllLiveReports() {
        log.info("모든 실시간 제보 조회");
        return liveReportRepository.findAll().stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();
    }

    /**
     * ID로 특정 실시간 제보를 조회합니다.
     * @param id 조회할 제보 ID
     * @return LiveReportResponseDTO
     */
    @Transactional(readOnly = true)
    public LiveReportResponseDTO getLiveReportById(Integer id) {
        log.info("ID로 실시간 제보 조회: {}", id);
        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("실시간 제보를 찾을 수 없습니다. ID: " + id));
        return LiveReportResponseDTO.fromEntity(liveReport);
    }

    /**
     * 최신 실시간 제보 목록을 조회합니다. (최대 20개)
     * @return LiveReportResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<LiveReportResponseDTO> getRecentLiveReports() {
        log.info("최신 실시간 제보 조회");
        return liveReportRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 특정 위치 근처의 실시간 제보 목록을 조회합니다.
     * @param longitude 경도
     * @param latitude 위도
     * @param range 검색 반경
     * @return LiveReportResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<LiveReportResponseDTO> getLiveReportsNearLocation(Double longitude, Double latitude, Double range) {
        log.info("위치 기반 실시간 제보 조회 - 위치: ({}, {}), 반경: {}", longitude, latitude, range);
        return liveReportRepository.findByLocationRange(longitude, latitude, range).stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 실시간 제보를 수정합니다.
     * @param id 수정할 제보 ID
     * @param request 제보 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 제보 정보
     */
    public LiveReportResponseDTO updateLiveReport(Integer id, LiveReportRequestDTO request, User user) {
        log.info("실시간 제보 수정 - ID: {}, 사용자: {}", id, user.getId());

        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("실시간 제보를 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!liveReport.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("작성자 또는 관리자만 제보를 수정할 수 있습니다.");
        }

        liveReport.setContents(request.getContents() != null && !request.getContents().trim().isEmpty()
                ? request.getContents() : liveReport.getContents());
        liveReport.setLongitude(request.getLongitude() != null
                ? request.getLongitude() : liveReport.getLongitude());
        liveReport.setLatitude(request.getLatitude() != null
                ? request.getLatitude() : liveReport.getLatitude());

        LiveReport savedReport = liveReportRepository.save(liveReport);
        log.info("실시간 제보 수정 완료");
        return LiveReportResponseDTO.fromEntity(savedReport);
    }

    /**
     * 실시간 제보를 삭제합니다.
     * @param id 삭제할 제보 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void deleteLiveReport(Integer id, User user) {
        log.info("실시간 제보 삭제 - ID: {}, 사용자: {}", id, user.getId());

        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("실시간 제보를 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!liveReport.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("작성자 또는 관리자만 제보를 삭제할 수 있습니다.");
        }

        liveReportRepository.deleteById(id);
        log.info("실시간 제보 삭제 완료");
    }

    /**
     * 실시간 제보에 '좋아요'를 추가합니다.
     * @param id '좋아요'할 제보 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 업데이트된 제보 정보
     */
    public LiveReportResponseDTO likeLiveReport(Integer id, User user) {
        log.info("사용자 {}가 실시간 제보 {}에 '좋아요'를 눌렀습니다.", user.getId(), id);

        LiveReport liveReport = liveReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("실시간 제보를 찾을 수 없습니다. ID: " + id));

        // 실제 구현에서는 사용자가 이미 '좋아요'를 눌렀는지 확인해야 합니다.
        // 현재는 단순히 '좋아요' 수를 증가시킵니다.
        liveReport.setLikeCount(liveReport.getLikeCount() + 1);

        LiveReport savedReport = liveReportRepository.save(liveReport);
        log.info("실시간 제보 '좋아요' 처리 완료");
        return LiveReportResponseDTO.fromEntity(savedReport);
    }

    /**
     * 실시간 제보를 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 LiveReportResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<LiveReportResponseDTO> searchLiveReports(String searchBy, String search, Integer page,
                                                                  Integer pageSize, String sortBy, String sortDirection) {
        log.info("실시간 제보 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "created" -> "createdAt";
            case "likes" -> "likeCount";
            default -> "id";
        };

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
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
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
                        log.warn("잘못된 작성자 ID 형식으로 검색: {}", search);
                        reportPage = Page.empty(pageable);
                    }
                    break;
                case "likes":
                    try {
                        Integer likeCount = Integer.parseInt(search.trim());
                        reportPage = liveReportRepository.findByLikeCountBetween(
                                likeCount - 5, likeCount + 5, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 '좋아요' 수 형식으로 검색: {}", search);
                        reportPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    reportPage = liveReportRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<LiveReportResponseDTO> result = reportPage.getContent().stream()
                .map(LiveReportResponseDTO::fromEntity)
                .toList();

        log.info("총 {}개의 실시간 제보 중 {}개를 찾았습니다.", reportPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, reportPage.getTotalElements(), reportPage.getTotalPages());
    }
}
