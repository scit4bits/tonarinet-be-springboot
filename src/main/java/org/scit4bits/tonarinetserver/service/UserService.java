package org.scit4bits.tonarinetserver.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.entity.Article;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserRole;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.repository.UserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * 액세스 토큰으로 사용자를 조회합니다.
     * @param accessToken JWT 액세스 토큰
     * @return 조회된 사용자 객체, 없으면 null
     */
    public User getUserByAccessToken(String accessToken) {
        try {
            // JWT 토큰 검증 및 디코딩
            DecodedJWT decodedJWT = JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256("JWTSecretKeyLOL"))
                    .build()
                    .verify(accessToken);

            Integer userId = Integer.parseInt(decodedJWT.getSubject());
            User user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                log.debug("User found by access token: {}", user.getEmail());
                return user;
            } else {
                log.warn("User not found for userId: {}", userId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error validating access token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 모든 사용자를 조회합니다.
     * @return 모든 사용자 정보 DTO 리스트
     */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    /**
     * 사용자의 관리자 권한을 토글합니다.
     * @param userId 권한을 변경할 사용자 ID
     */
    public void toggleAdmin(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsAdmin(!user.getIsAdmin());
        userRepository.save(user);
    }

    /**
     * 사용자를 검색합니다.
     * @param searchBy 검색 기준 (id, email, name 등)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준 필드
     * @param sortDirection 정렬 방향 (asc, desc)
     * @return 페이징된 사용자 검색 결과
     */
    public PagedResponse<UserDTO> searchUser(String searchBy, String search, Integer page, Integer pageSize,
                                             String sortBy, String sortDirection) {
        log.info("Searching users with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // sortBy 필드명 매핑 (엔티티 필드명과 일치하도록)
        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "email" -> "email";
            case "name" -> "name";
            case "nickname" -> "nickname";
            case "phone" -> "phone";
            case "birth" -> "birth";
            case "nationality" -> "nationality.countryCode";
            case "isadmin" -> "isAdmin";
            default -> "id"; // 기본값
        };

        // 정렬 및 페이징 설정
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<User> userPage;

        // searchBy에 따른 검색 로직
        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없으면 모든 사용자 조회
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = switch (searchBy.toLowerCase()) {
                case "all" -> userRepository.findByAllFieldsContaining(search.trim(), pageable);
                case "id" -> {
                    try {
                        Integer searchId = Integer.valueOf(search.trim());
                        yield userRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        yield Page.empty(pageable);
                    }
                }
                case "email" -> userRepository.findByEmailContainingIgnoreCase(search.trim(), pageable);
                case "name" -> userRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
                case "nickname" -> userRepository.findByNicknameContainingIgnoreCase(search.trim(), pageable);
                case "phone" -> userRepository.findByPhoneContainingIgnoreCase(search.trim(), pageable);
                case "nationality" -> userRepository.findByNationality_CountryCodeContainingIgnoreCase(search.trim(),
                        pageable);
                case "isadmin" -> {
                    try {
                        Boolean isAdmin = Boolean.valueOf(search.trim().toLowerCase());
                        yield userRepository.findByIsAdmin(isAdmin, pageable);
                    } catch (Exception e) {
                        log.warn("Invalid boolean format for isAdmin search: {}", search);
                        yield Page.empty(pageable);
                    }
                }
                default ->
                    // 기본적으로 전체 검색 수행
                        userRepository.findByAllFieldsContaining(search.trim(), pageable);
            };
        }

        // Entity를 DTO로 변환하여 반환
        int totalPages = userPage.getTotalPages();
        long totalCount = userPage.getTotalElements();

        List<UserDTO> result = userPage.getContent().stream()
                .map(UserDTO::fromEntity)
                .toList();

        log.info("Found {} users out of {} total", result.size(), userPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, totalCount, totalPages);
    }

    /**
     * 조직의 멤버들을 검색합니다.
     *
     * @param organizationId 조직 ID
     * @param searchBy       검색 기준 (all, id, email, name, nickname, phone, nationality, isadmin, isgranted)
     * @param search         검색어
     * @param page           페이지 번호 (0부터 시작)
     * @param pageSize       페이지 크기
     * @param sortBy         정렬 기준
     * @param sortDirection  정렬 방향 (asc, desc)
     * @return 페이징된 조직 멤버 검색 결과
     */
    public PagedResponse<UserDTO> searchOrganizationMembers(Integer organizationId, String searchBy, String search,
                                                            Integer page, Integer pageSize, String sortBy, String sortDirection) {
        log.info(
                "Searching organization members with organizationId: {}, searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
                organizationId, searchBy, search, page, pageSize, sortBy, sortDirection);

        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // sortBy 필드명 매핑
        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "email" -> "email";
            case "name" -> "name";
            case "nickname" -> "nickname";
            case "phone" -> "phone";
            case "birth" -> "birth";
            case "nationality" -> "nationality.countryCode";
            case "isadmin" -> "isAdmin";
            case "isgranted" -> "isGranted";
            default -> "id"; // 기본값
        };

        // 정렬 및 페이징 설정
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<User> userPage;

        // searchBy에 따른 검색 로직
        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없으면 해당 조직의 모든 멤버 조회
            userPage = userRepository.findByOrganizationId(organizationId, pageable);
        } else {
            userPage = switch (searchBy.toLowerCase()) {
                case "all" -> userRepository.findByOrganizationIdAndAllFieldsContaining(organizationId, search.trim(),
                        pageable);
                case "id" -> {
                    try {
                        Integer searchId = Integer.valueOf(search.trim());
                        yield userRepository.findByOrganizationIdAndId(organizationId, searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        yield Page.empty(pageable);
                    }
                }
                case "email" -> userRepository.findByOrganizationIdAndEmailContainingIgnoreCase(organizationId,
                        search.trim(), pageable);
                case "name" -> userRepository.findByOrganizationIdAndNameContainingIgnoreCase(organizationId,
                        search.trim(), pageable);
                case "nickname" -> userRepository.findByOrganizationIdAndNicknameContainingIgnoreCase(organizationId,
                        search.trim(), pageable);
                case "phone" -> userRepository.findByOrganizationIdAndPhoneContainingIgnoreCase(organizationId,
                        search.trim(), pageable);
                case "nationality" -> userRepository.findByOrganizationIdAndNationality_CountryCodeContainingIgnoreCase(
                        organizationId, search.trim(), pageable);
                case "isadmin" -> {
                    try {
                        Boolean isAdmin = Boolean.valueOf(search.trim().toLowerCase());
                        yield userRepository.findByOrganizationIdAndIsAdmin(organizationId, isAdmin, pageable);
                    } catch (Exception e) {
                        log.warn("Invalid boolean format for isAdmin search: {}", search);
                        yield Page.empty(pageable);
                    }
                }
                default ->
                    // 기본적으로 전체 검색 수행
                        userRepository.findByOrganizationIdAndAllFieldsContaining(organizationId, search.trim(),
                                pageable);
            };
        }

        // Entity를 DTO로 변환하여 반환
        int totalPages = userPage.getTotalPages();
        long totalCount = userPage.getTotalElements();

        List<UserDTO> result = new ArrayList<>();

        for (User user : userPage.getContent()) {
            UserDTO userDTO = UserDTO.fromEntity(user);

            UserRole userRole = userRoleRepository
                    .findById(UserRole.UserRoleId.builder().userId(user.getId()).orgId(organizationId).build()).orElseThrow(() -> new RuntimeException("UserRole not found"));
            userDTO.setEntryMessage(userRole.getEntryMessage());
            userDTO.setIsGranted(userRole.getIsGranted());
            userDTO.setRole(userRole.getRole());
            result.add(userDTO);
        }

        log.info("Found {} organization members out of {} total", result.size(), userPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, totalCount, totalPages);
    }

    /**
     * 조직 내 사용자의 역할을 토글합니다. (승인/미승인)
     * @param user 현재 로그인한 관리자 사용자
     * @param userId 역할을 변경할 사용자 ID
     * @param orgId 조직 ID
     */
    public void toggleUserRole(User user, Integer userId, Integer orgId) {
        UserRole.UserRoleId adminUserRoleId = UserRole.UserRoleId.builder()
                .userId(user.getId())
                .orgId(orgId)
                .build();

        UserRole adminUserRole = userRoleRepository.findById(adminUserRoleId)
                .orElseThrow(() -> new RuntimeException("Admin UserRole not found"));

        // 관리자 권한 확인
        if (!user.getIsAdmin() && !adminUserRole.getRole().equals("admin")) {
            throw new RuntimeException("User is not an admin");
        }

        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(userId)
                .orgId(orgId)
                .build();

        UserRole userRole = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new RuntimeException("UserRole not found"));

        userRole.setIsGranted(!userRole.getIsGranted());
        userRoleRepository.save(userRole);
    }

    /**
     * 조직 내 사용자의 역할을 변경합니다.
     * @param user 현재 로그인한 관리자 사용자
     * @param userId 역할을 변경할 사용자 ID
     * @param orgId 조직 ID
     * @param newRole 새로운 역할
     */
    public void changeUserRole(User user, Integer userId, Integer orgId, String newRole) {

        UserRole.UserRoleId adminUserRoleId = UserRole.UserRoleId.builder()
                .userId(user.getId())
                .orgId(orgId)
                .build();

        UserRole adminUserRole = userRoleRepository.findById(adminUserRoleId)
                .orElseThrow(() -> new RuntimeException("Admin UserRole not found"));

        // 관리자 권한 확인
        if (!user.getIsAdmin() && !adminUserRole.getRole().equals("admin")) {
            throw new RuntimeException("User is not an admin");
        }

        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(userId)
                .orgId(orgId)
                .build();

        UserRole userRole = userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new RuntimeException("UserRole not found"));

        userRole.setRole(newRole);
        userRoleRepository.save(userRole);
    }

    /**
     * 내가 작성한 상담 게시글 목록을 조회합니다.
     * @param user 현재 로그인한 사용자
     * @return 사용자가 작성한 상담 게시글 DTO 리스트
     */
    public List<ArticleDTO> getMyCounsels(User user) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));

        List<Article> counsels = dbUser.getArticles();

        // 카테고리가 'counsel'인 게시글만 필터링
        counsels = counsels.stream()
                .filter(article -> article.getCategory().equals("counsel"))
                .toList();

        return counsels.stream()
                .map(ArticleDTO::fromEntity)
                .toList();
    }

    public void changeProfileImage(User user, Integer fileId) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));
        dbUser.setProfileFileId(fileId);
        userRepository.save(dbUser);
    }
}
