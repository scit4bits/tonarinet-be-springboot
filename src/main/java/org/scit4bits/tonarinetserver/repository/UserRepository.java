package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByOauthidAndProvider(String oauthid, String provider);

    boolean existsByEmail(String email);

    // ID로 검색
    Page<User> findById(Integer id, Pageable pageable);

    // 이메일로 검색
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // 이름으로 검색
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 닉네임으로 검색
    Page<User> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);

    // 전화번호로 검색
    Page<User> findByPhoneContainingIgnoreCase(String phone, Pageable pageable);

    // 국적으로 검색
    Page<User> findByNationality_CountryCodeContainingIgnoreCase(String countryCode, Pageable pageable);

    // 관리자 여부로 검색
    Page<User> findByIsAdmin(Boolean isAdmin, Pageable pageable);

    // 전체 검색을 위한 커스텀 쿼리 (모든 필드를 검색)
    @Query("SELECT u FROM User u WHERE " +
            "CAST(u.id AS string) LIKE CONCAT('%', :search, '%') OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nationality.countryCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    // 조직의 멤버들을 조회하는 쿼리들 (isGranted 값과 관계없이 UserRole이 있는 모든 사용자)
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId")
    Page<User> findByOrganizationId(@Param("organizationId") Integer organizationId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND u.id = :userId")
    Page<User> findByOrganizationIdAndId(@Param("organizationId") Integer organizationId, @Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<User> findByOrganizationIdAndEmailContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("email") String email, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByOrganizationIdAndNameContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("name") String name, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%'))")
    Page<User> findByOrganizationIdAndNicknameContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("nickname") String nickname, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.phone) LIKE LOWER(CONCAT('%', :phone, '%'))")
    Page<User> findByOrganizationIdAndPhoneContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("phone") String phone, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.nationality.countryCode) LIKE LOWER(CONCAT('%', :countryCode, '%'))")
    Page<User> findByOrganizationIdAndNationality_CountryCodeContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("countryCode") String countryCode, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND u.isAdmin = :isAdmin")
    Page<User> findByOrganizationIdAndIsAdmin(@Param("organizationId") Integer organizationId, @Param("isAdmin") Boolean isAdmin, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND (" +
            "CAST(u.id AS string) LIKE CONCAT('%', :search, '%') OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nationality.countryCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByOrganizationIdAndAllFieldsContaining(@Param("organizationId") Integer organizationId, @Param("search") String search, Pageable pageable);
}
