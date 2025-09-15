package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.LiveReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 실시간 제보(LiveReport) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface LiveReportRepository extends JpaRepository<LiveReport, Integer> {

    /**
     * ID로 실시간 제보를 페이징하여 조회합니다.
     * @param id 제보 ID
     * @param pageable 페이징 정보
     * @return 페이징된 실시간 제보
     */
    Page<LiveReport> findById(Integer id, Pageable pageable);

    /**
     * 내용에 특정 문자열을 포함하는 실시간 제보를 페이징하여 조회합니다. (대소문자 무시)
     * @param contents 검색할 내용 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 실시간 제보
     */
    Page<LiveReport> findByContentsContainingIgnoreCase(String contents, Pageable pageable);

    /**
     * 작성자 ID로 실시간 제보를 페이징하여 조회합니다.
     * @param createdById 작성자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 실시간 제보
     */
    Page<LiveReport> findByCreatedById(Integer createdById, Pageable pageable);

    /**
     * 최신 20개의 실시간 제보를 생성 시간 내림차순으로 조회합니다.
     * @return 최신 실시간 제보 리스트
     */
    List<LiveReport> findTop20ByOrderByCreatedAtDesc();

    /**
     * 좋아요 수가 특정 범위 내에 있는 실시간 제보를 페이징하여 조회합니다.
     * @param minLikes 최소 좋아요 수
     * @param maxLikes 최대 좋아요 수
     * @param pageable 페이징 정보
     * @return 페이징된 실시간 제보
     */
    Page<LiveReport> findByLikeCountBetween(Integer minLikes, Integer maxLikes, Pageable pageable);

    /**
     * 특정 위치 근처의 오늘 작성된 실시간 제보를 좋아요 순으로 조회합니다.
     * @param longitude 경도
     * @param latitude 위도
     * @param range 검색 범위
     * @return 근처의 실시간 제보 리스트
     */
    @Query("SELECT lr FROM LiveReport lr WHERE " +
            "ABS(lr.longitude - :longitude) <= :range AND " +
            "ABS(lr.latitude - :latitude) <= :range AND " +
            "DATE(lr.createdAt) = CURRENT_DATE " +
            "ORDER BY lr.likeCount DESC")
    List<LiveReport> findByLocationRange(@Param("longitude") Double longitude, @Param("latitude") Double latitude, @Param("range") Double range);

    /**
     * 모든 필드(내용)에서 검색어와 일치하는 실시간 제보를 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 실시간 제보
     */
    @Query("SELECT lr FROM LiveReport lr WHERE " +
            "LOWER(lr.contents) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<LiveReport> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
