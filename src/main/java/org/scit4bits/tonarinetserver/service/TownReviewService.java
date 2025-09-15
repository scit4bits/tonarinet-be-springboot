package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TownReviewRequestDTO;
import org.scit4bits.tonarinetserver.dto.TownReviewResponseDTO;
import org.scit4bits.tonarinetserver.entity.Region;
import org.scit4bits.tonarinetserver.entity.TownReview;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.RegionRepository;
import org.scit4bits.tonarinetserver.repository.TownReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 동네 리뷰 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TownReviewService {

    private final TownReviewRepository townReviewRepository;
    private final RegionRepository regionRepository;

    /**
     * 새로운 동네 리뷰를 생성합니다.
     * @param request 리뷰 생성 요청 정보
     * @param creator 작성자 정보
     * @return 생성된 리뷰 정보
     */
    public TownReviewResponseDTO createTownReview(TownReviewRequestDTO request, User creator) {
        log.info("동네 리뷰 생성 - 지역: {}, 작성자: {}", request.getRegionId(), creator.getId());

        TownReview townReview = TownReview.builder()
                .contents(request.getContents())
                .createdById(creator.getId())
                .transportation(request.getTransportation())
                .safety(request.getSafety())
                .infra(request.getInfra())
                .population(request.getPopulation())
                .education(request.getEducation())
                .regionId(request.getRegionId())
                .likeCount(0)
                .build();

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new RuntimeException("지역을 찾을 수 없습니다. ID: " + request.getRegionId()));

        townReview.setCountryCode(region.getCountryCode());

        TownReview savedReview = townReviewRepository.save(townReview);
        log.info("동네 리뷰 생성 완료, ID: {}", savedReview.getId());
        return TownReviewResponseDTO.fromEntity(savedReview);
    }

    /**
     * 모든 동네 리뷰 목록을 조회합니다.
     * @return TownReviewResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getAllTownReviews() {
        log.info("모든 동네 리뷰 조회");
        return townReviewRepository.findAll().stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    /**
     * ID로 특정 동네 리뷰를 조회합니다.
     * @param id 조회할 리뷰 ID
     * @return TownReviewResponseDTO
     */
    @Transactional(readOnly = true)
    public TownReviewResponseDTO getTownReviewById(Integer id) {
        log.info("ID로 동네 리뷰 조회: {}", id);
        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("동네 리뷰를 찾을 수 없습니다. ID: " + id));
        return TownReviewResponseDTO.fromEntity(townReview);
    }

    /**
     * 특정 지역의 모든 동네 리뷰를 조회합니다.
     * @param regionId 지역 ID
     * @return TownReviewResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getTownReviewsByRegionId(Integer regionId) {
        log.info("지역 {}의 동네 리뷰 조회", regionId);
        return townReviewRepository.findByRegionIdOrderByLikeCountDesc(regionId).stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 특정 국가의 모든 동네 리뷰를 조회합니다.
     * @param countryCode 국가 코드
     * @return TownReviewResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TownReviewResponseDTO> getTownReviewsByCountryCode(String countryCode) {
        log.info("국가 {}의 동네 리뷰 조회", countryCode);
        return townReviewRepository.findByCountryCodeOrderByLikeCountDesc(countryCode).stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 동네 리뷰를 수정합니다.
     * @param id 수정할 리뷰 ID
     * @param request 리뷰 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 리뷰 정보
     */
    public TownReviewResponseDTO updateTownReview(Integer id, TownReviewRequestDTO request, User user) {
        log.info("동네 리뷰 수정 - ID: {}, 사용자: {}", id, user.getId());

        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("동네 리뷰를 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!townReview.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("리뷰 작성자 또는 관리자만 수정할 수 있습니다.");
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

        TownReview savedReview = townReviewRepository.save(townReview);
        log.info("동네 리뷰 수정 완료");
        return TownReviewResponseDTO.fromEntity(savedReview);
    }

    /**
     * 동네 리뷰를 삭제합니다.
     * @param id 삭제할 리뷰 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void deleteTownReview(Integer id, User user) {
        log.info("동네 리뷰 삭제 - ID: {}, 사용자: {}", id, user.getId());

        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("동네 리뷰를 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!townReview.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("리뷰 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        townReviewRepository.deleteById(id);
        log.info("동네 리뷰 삭제 완료");
    }

    /**
     * 동네 리뷰에 '좋아요'를 추가합니다.
     * @param id '좋아요'할 리뷰 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 업데이트된 리뷰 정보
     */
    public TownReviewResponseDTO likeTownReview(Integer id, User user) {
        log.info("사용자 {}가 동네 리뷰 {}에 '좋아요'를 눌렀습니다.", user.getId(), id);

        TownReview townReview = townReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("동네 리뷰를 찾을 수 없습니다. ID: " + id));

        // 실제 구현에서는 사용자가 이미 '좋아요'를 눌렀는지 확인해야 합니다.
        // 현재는 단순히 '좋아요' 수를 증가시킵니다.
        townReview.setLikeCount(townReview.getLikeCount() + 1);

        TownReview savedReview = townReviewRepository.save(townReview);
        log.info("동네 리뷰 '좋아요' 처리 완료");
        return TownReviewResponseDTO.fromEntity(savedReview);
    }

    /**
     * 동네 리뷰를 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 TownReviewResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<TownReviewResponseDTO> searchTownReviews(String searchBy, String search, Integer page,
                                                                  Integer pageSize, String sortBy, String sortDirection) {
        log.info("동네 리뷰 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
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
            case "region" -> "regionId";
            case "country" -> "countryCode";
            default -> "id";
        };

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
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
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
                        log.warn("잘못된 작성자 ID 형식으로 검색: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                case "region":
                    try {
                        Integer regionId = Integer.parseInt(search.trim());
                        reviewPage = townReviewRepository.findByRegionId(regionId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 지역 ID 형식으로 검색: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                case "country":
                    reviewPage = townReviewRepository.findByCountryCode(search.trim(), pageable);
                    break;
                case "rating":
                    try {
                        double rating = Double.parseDouble(search.trim());
                        // 0.5 범위 내의 평균 평점을 가진 리뷰 검색
                        reviewPage = townReviewRepository.findByAverageRatingBetween(
                                rating - 0.5, rating + 0.5, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 평점 형식으로 검색: {}", search);
                        reviewPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    reviewPage = townReviewRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<TownReviewResponseDTO> result = reviewPage.getContent().stream()
                .map(TownReviewResponseDTO::fromEntity)
                .toList();

        log.info("총 {}개의 동네 리뷰 중 {}개를 찾았습니다.", reviewPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, reviewPage.getTotalElements(), reviewPage.getTotalPages());
    }
}
