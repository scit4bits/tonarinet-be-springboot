package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.TownReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TownReviewRepository extends JpaRepository<TownReview, Integer> {
    
    // ID로 검색
    Page<TownReview> findById(Integer id, Pageable pageable);
    
    // 내용으로 검색
    Page<TownReview> findByContentsContainingIgnoreCase(String contents, Pageable pageable);
    
    // 생성자 ID로 검색
    Page<TownReview> findByCreatedById(Integer createdById, Pageable pageable);
    
    // 지역 ID로 검색
    Page<TownReview> findByRegionId(Integer regionId, Pageable pageable);
    
    // 국가 코드로 검색
    Page<TownReview> findByCountryCode(String countryCode, Pageable pageable);
    
    // 특정 지역의 리뷰들 (좋아요 순)
    List<TownReview> findByRegionIdOrderByLikeCountDesc(Integer regionId);
    
    // 특정 국가의 리뷰들 (좋아요 순)
    List<TownReview> findByCountryCodeOrderByLikeCountDesc(String countryCode);
    
    // 평점 범위로 검색 (교통, 안전, 인프라, 인구, 교육 평균)
    @Query("SELECT tr FROM TownReview tr WHERE " +
           "((tr.transportation + tr.safety + tr.infra + tr.population + tr.education) / 5.0) >= :minRating AND " +
           "((tr.transportation + tr.safety + tr.infra + tr.population + tr.education) / 5.0) <= :maxRating")
    Page<TownReview> findByAverageRatingBetween(@Param("minRating") Double minRating, @Param("maxRating") Double maxRating, Pageable pageable);
    
    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT tr FROM TownReview tr WHERE " +
           "LOWER(tr.contents) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(tr.countryCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<TownReview> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
