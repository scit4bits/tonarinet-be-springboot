package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자(User) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * 이메일로 사용자를 조회합니다.
     * @param email 사용자 이메일
     * @return {@link Optional}{@code <}{@link User}{@code >}
     */
    Optional<User> findByEmail(String email);

    /**
     * OAuth ID와 제공자로 사용자를 조회합니다.
     * @param oauthid OAuth ID
     * @param provider OAuth 제공자
     * @return {@link Optional}{@code <}{@link User}{@code >}
     */
    Optional<User> findByOauthidAndProvider(String oauthid, String provider);

    /**
     * 해당 이메일이 존재하는지 확인합니다.
     * @param email 확인할 이메일
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByEmail(String email);

    /**
     * ID로 사용자를 페이징하여 조회합니다.
     * @param id 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    Page<User> findById(Integer id, Pageable pageable);

    /**
     * 이메일에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param email 검색할 이메일 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * 이름에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param name 검색할 이름 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 닉네임에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param nickname 검색할 닉네임 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    Page<User> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);

    /**
     * 전화번호에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param phone 검색할 전화번호 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    Page<User> findByPhoneContainingIgnoreCase(String phone, Pageable pageable);

    /**
     * 국적 코드에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param countryCode 검색할 국적 코드 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    Page<User> findByNationality_CountryCodeContainingIgnoreCase(String countryCode, Pageable pageable);

    /**
     * 관리자 여부로 사용자를 페이징하여 조회합니다.
     * @param isAdmin 관리자 여부
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    Page<User> findByIsAdmin(Boolean isAdmin, Pageable pageable);

    /**
     * 모든 필드에서 검색어와 일치하는 사용자를 페이징하여 조회합니다.
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u WHERE " +
            "CAST(u.id AS string) LIKE CONCAT('%', :search, '%') OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nationality.countryCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByAllFieldsContaining(@Param("search") String search, Pageable pageable);

    /**
     * 특정 조직에 속한 모든 사용자를 페이징하여 조회합니다.
     * @param organizationId 조직 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId")
    Page<User> findByOrganizationId(@Param("organizationId") Integer organizationId, Pageable pageable);

    /**
     * 특정 조직의 특정 ID를 가진 사용자를 페이징하여 조회합니다.
     * @param organizationId 조직 ID
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND u.id = :userId")
    Page<User> findByOrganizationIdAndId(@Param("organizationId") Integer organizationId, @Param("userId") Integer userId, Pageable pageable);

    /**
     * 특정 조직 내에서 이메일에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다.
     * @param organizationId 조직 ID
     * @param email 검색할 이메일 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<User> findByOrganizationIdAndEmailContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("email") String email, Pageable pageable);

    /**
     * 특정 조직 내에서 이름에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param organizationId 조직 ID
     * @param name 검색할 이름 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByOrganizationIdAndNameContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("name") String name, Pageable pageable);

    /**
     * 특정 조직 내에서 닉네임에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param organizationId 조직 ID
     * @param nickname 검색할 닉네임 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%'))")
    Page<User> findByOrganizationIdAndNicknameContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("nickname") String nickname, Pageable pageable);

    /**
     * 특정 조직 내에서 전화번호에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param organizationId 조직 ID
     * @param phone 검색할 전화번호 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.phone) LIKE LOWER(CONCAT('%', :phone, '%'))")
    Page<User> findByOrganizationIdAndPhoneContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("phone") String phone, Pageable pageable);

    /**
     * 특정 조직 내에서 국적 코드에 특정 문자열을 포함하는 사용자를 페이징하여 조회합니다. (대소문자 무시)
     * @param organizationId 조직 ID
     * @param countryCode 검색할 국적 코드 문자열
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND LOWER(u.nationality.countryCode) LIKE LOWER(CONCAT('%', :countryCode, '%'))")
    Page<User> findByOrganizationIdAndNationality_CountryCodeContainingIgnoreCase(@Param("organizationId") Integer organizationId, @Param("countryCode") String countryCode, Pageable pageable);

    /**
     * 관리자 여부로 사용자를 페이징하여 조회합니다.
     * @param organizationId 조직 ID
     * @param isAdmin 관리자 여부
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND u.isAdmin = :isAdmin")
    Page<User> findByOrganizationIdAndIsAdmin(@Param("organizationId") Integer organizationId, @Param("isAdmin") Boolean isAdmin, Pageable pageable);

    /**
     * 특정 조직 내에서 모든 필드에서 검색어와 일치하는 사용자를 페이징하여 조회합니다.
     * @param organizationId 조직 ID
     * @param search 검색어
     * @param pageable 페이징 정보
     * @return 페이징된 사용자
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur WHERE ur.organization.id = :organizationId AND (" +
            "CAST(u.id AS string) LIKE CONCAT('%', :search, '%') OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.nationality.countryCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByOrganizationIdAndAllFieldsContaining(@Param("organizationId") Integer organizationId, @Param("search") String search, Pageable pageable);
}

