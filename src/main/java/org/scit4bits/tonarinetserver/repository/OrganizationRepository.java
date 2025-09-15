package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 조직(Organization) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    /**
     * 이름으로 조직을 조회합니다.
     * @param name 조직 이름
     * @return {@link Optional}{@link Organization}
     */
    Optional<Organization> findByName(String name);

    /**
     * 이름에 특정 문자열을 포함하는 조직 리스트를 조회합니다. (대소문자 무시)
     * @param name 검색할 조직 이름
     * @return 조직 리스트
     */
    List<Organization> findByNameContainingIgnoreCase(String name);

    /**
     * ID로 조직을 페이징하여 조회합니다。
     * @param id 조직 ID
     * @param pageable 페이징 정보
     * @return 페이징된 조직
     */
    Page<Organization> findById(Integer id, Pageable pageable);

    /**
     * 이름에 특정 문자열을 포함하는 조직을 페이징하여 조회합니다. (대소문자 무시)
     * @param name 검색할 조직 이름
     * @param pageable 페이징 정보
     * @return 페이징된 조직
     */
    Page<Organization> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 국가 코드에 특정 문자열을 포함하는 조직을 페이징하여 조회합니다. (대소문자 무시)
     * @param countryCode 검색할 국가 코드
     * @param pageable 페이징 정보
     * @return 페이징된 조직
     */
    Page<Organization> findByCountryCodeContainingIgnoreCase(String countryCode, Pageable pageable);

    /**
     * 타입에 특정 문자열을 포함하는 조직을 페이징하여 조회합니다. (대소문자 무시)
     * @param type 검색할 조직 타입
     * @param pageable 페이징 정보
     * @return 페이징된 조직
     */
    Page<Organization> findByTypeContainingIgnoreCase(String type, Pageable pageable);

    /**
     * 모든 필드(이름, 국가코드, 타입)에서 검색어와 일치하는 조직을 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 조직
     */
    @Query("SELECT o FROM Organization o WHERE " +
            "LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.countryCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.type) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Organization> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}

