package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.LiveReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveReportRepository extends JpaRepository<LiveReport, Integer> {

    // ID로 검색
    Page<LiveReport> findById(Integer id, Pageable pageable);

    // 내용으로 검색
    Page<LiveReport> findByContentsContainingIgnoreCase(String contents, Pageable pageable);

    // 생성자 ID로 검색
    Page<LiveReport> findByCreatedById(Integer createdById, Pageable pageable);

    // 좋아요 수로 정렬된 최신 리포트들
    List<LiveReport> findTop20ByOrderByCreatedAtDesc();

    // 좋아요 수 범위로 검색
    Page<LiveReport> findByLikeCountBetween(Integer minLikes, Integer maxLikes, Pageable pageable);

    // 특정 위치 근처의 리포트들 (간단한 범위 검색)
    @Query("SELECT lr FROM LiveReport lr WHERE " +
            "ABS(lr.longitude - :longitude) <= :range AND " +
            "ABS(lr.latitude - :latitude) <= :range AND " +
            "DATE(lr.createdAt) = CURRENT_DATE " +
            "ORDER BY lr.likeCount DESC")
    List<LiveReport> findByLocationRange(@Param("longitude") Double longitude, @Param("latitude") Double latitude, @Param("range") Double range);

    // 전체 검색을 위한 커스텀 쿼리
    @Query("SELECT lr FROM LiveReport lr WHERE " +
            "LOWER(lr.contents) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<LiveReport> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
