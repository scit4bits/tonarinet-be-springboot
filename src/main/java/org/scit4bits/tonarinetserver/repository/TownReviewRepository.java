package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.TownReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 동네 리뷰(TownReview) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface TownReviewRepository extends JpaRepository<TownReview, Integer> {

    /**
     * ID로 동네 리뷰를 페이징하여 조회합니다.
     * @param id 리뷰 ID
     * @param pageable 페이징 정보
     * @return 페이징된 동네 리뷰
     */
    Page<TownReview> findById(Integer id, Pageable pageable);

    /**
     * 내용에 특정 문자열을 포함하는 동네 리뷰를 페이징하여 조회합니다. (대소문자 무시)
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 동네 리뷰
     */
    Page<TownReview> findByContentsContainingIgnoreCase(String contents, Pageable pageable);

    /**
     * 작성자 ID로 동네 리뷰를 페이징하여 조회합니다.
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 동네 리뷰
     */
    Page<TownReview> findByCreatedById(Integer createdById, Pageable pageable);

    /**
     * 지역 ID로 동네 리뷰를 페이징하여 조회합니다.
     * @param regionId 지역 ID
     * @param pageable 페이징 정보
     * @return 페이징된 동네 리뷰
     */
    Page<TownReview> findByRegionId(Integer regionId, Pageable pageable);

    /**
     * 국가 코드로 동네 리뷰를 페이징하여 조회합니다.
     * @param countryCode 국가 코드
     * @param pageable 페이징 정보
     * @return 페이징된 동네 리뷰
     */
    Page<TownReview> findByCountryCode(String countryCode, Pageable pageable);

    /**
     * 특정 지역의 모든 리뷰를 좋아요 순으로 조회합니다.
     * @param regionId 지역 ID
     * @return 좋아요 순으로 정렬된 리뷰 리스트
     */
    List<TownReview> findByRegionIdOrderByLikeCountDesc(Integer regionId);

    /**
     * 특정 국가의 모든 리뷰를 좋아요 순으로 조회합니다.
     * @param countryCode 국가 코드
     * @return 좋아요 순으로 정렬된 리뷰 리스트
     */
    List<TownReview> findByCountryCodeOrderByLikeCountDesc(String countryCode);

    /**
     * 평균 평점 범위로 동네 리뷰를 페이징하여 조회합니다.
     * @param minRating 최소 평균 평점
     * @param maxRating 최대 평균 평점
     * @param pageable 페이징 정보
     * @return 페이징된 동네 리뷰
     */
    @Query("SELECT tr FROM TownReview tr WHERE " +
            "((tr.transportation + tr.safety + tr.infra + tr.population + tr.education) / 5.0) >= :minRating AND " +
            "((tr.transportation + tr.safety + tr.infra + tr.population + tr.education) / 5.0) <= :maxRating")
    Page<TownReview> findByAverageRatingBetween(@Param("minRating") Double minRating, @Param("maxRating") Double maxRating, Pageable pageable);

    /**
     * 모든 필드(내용, 국가 코드)에서 검색어와 일치하는 동네 리뷰를 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 동네 리뷰
     */
    @Query("SELECT tr FROM TownReview tr WHERE " +
            "LOWER(tr.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(tr.countryCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<TownReview> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
