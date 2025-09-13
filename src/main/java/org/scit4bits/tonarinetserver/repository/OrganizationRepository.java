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

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> findByName(String name);

    List<Organization> findByNameContainingIgnoreCase(String name);

    // ID로 검색
    Page<Organization> findById(Integer id, Pageable pageable);

    // 이름으로 검색
    Page<Organization> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 국가로 검색
    Page<Organization> findByCountryCodeContainingIgnoreCase(String countryCode, Pageable pageable);

    // 타입으로 검색
    Page<Organization> findByTypeContainingIgnoreCase(String type, Pageable pageable);

    // 전체 검색을 위한 커스텀 쿼리 (이름, 국가코드, 타입을 모두 검색)
    @Query("SELECT o FROM Organization o WHERE " +
            "LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.countryCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.type) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Organization> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);
}
