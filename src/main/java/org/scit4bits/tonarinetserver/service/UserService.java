package org.scit4bits.tonarinetserver.service;

import java.util.ArrayList;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.UserDTO;
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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public User getUserByAccessToken(String accessToken){
        try {
            DecodedJWT decodedJWT = JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256("JWTSecretKeyLOL"))
                .build()
                .verify(accessToken);
            
            Integer userId = Integer.parseInt(decodedJWT.getSubject());
            User user = userRepository.findById(userId).get();
            
            if(user != null) {
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

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    public void toggleAdmin(Integer userId) {
        User user = userRepository.findById(userId).get();
        user.setIsAdmin(!user.getIsAdmin());
        userRepository.save(user);
    }

    public PagedResponse<UserDTO> searchUser(String searchBy, String search, Integer page, Integer pageSize, String sortBy, String sortDirection) {
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
        String entityFieldName;
        switch (sortByField.toLowerCase()) {
            case "id":
                entityFieldName = "id";
                break;
            case "email":
                entityFieldName = "email";
                break;
            case "name":
                entityFieldName = "name";
                break;
            case "nickname":
                entityFieldName = "nickname";
                break;
            case "phone":
                entityFieldName = "phone";
                break;
            case "birth":
                entityFieldName = "birth";
                break;
            case "nationality":
                entityFieldName = "nationality.countryCode";
                break;
            case "isadmin":
                entityFieldName = "isAdmin";
                break;
            default:
                entityFieldName = "id"; // 기본값
                break;
        }
        
        // 정렬 및 페이징 설정
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<User> userPage;
        
        // searchBy에 따른 검색 로직
        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없으면 모든 사용자 조회
            userPage = userRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    userPage = userRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.valueOf(search.trim());
                        userPage = userRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        userPage = Page.empty(pageable);
                    }
                    break;
                case "email":
                    userPage = userRepository.findByEmailContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "name":
                    userPage = userRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "nickname":
                    userPage = userRepository.findByNicknameContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "phone":
                    userPage = userRepository.findByPhoneContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "nationality":
                    userPage = userRepository.findByNationality_CountryCodeContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "isadmin":
                    try {
                        Boolean isAdmin = Boolean.valueOf(search.trim().toLowerCase());
                        userPage = userRepository.findByIsAdmin(isAdmin, pageable);
                    } catch (Exception e) {
                        log.warn("Invalid boolean format for isAdmin search: {}", search);
                        userPage = Page.empty(pageable);
                    }
                    break;
                default:
                    // 기본적으로 전체 검색 수행
                    userPage = userRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }
        
        // Entity를 DTO로 변환하여 반환
        int totalPages = userPage.getTotalPages();
        long totalCount = userPage.getTotalElements();

        List<UserDTO> result = userPage.getContent().stream()
                .map(UserDTO::fromEntity)
                .toList();
        
        log.info("Found {} users out of {} total", result.size(), userPage.getTotalElements());
        return new PagedResponse<UserDTO>(result, pageNum, pageSizeNum, totalCount, totalPages);
    }



    /**
     * 조직의 멤버들을 검색하는 메서드 (isGranted 필터 포함)
     * @param organizationId 조직 ID
     * @param searchBy 검색 기준 (all, id, email, name, nickname, phone, nationality, isadmin, isgranted)
     * @param search 검색어
     * @param page 페이지 번호 (0부터 시작)
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향 (asc, desc)
     * @param isGranted 승인 여부 필터 (null이면 모든 상태, true면 승인된 사용자만, false면 승인되지 않은 사용자만)
     * @return PagedResponse<UserDTO>
     */
    public PagedResponse<UserDTO> searchOrganizationMembers(Integer organizationId, String searchBy, String search, Integer page, Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching organization members with organizationId: {}, searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}, isGranted: {}", 
                organizationId, searchBy, search, page, pageSize, sortBy, sortDirection);
        
        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";
        
        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // sortBy 필드명 매핑 (엔티티 필드명과 일치하도록)
        String entityFieldName;
        switch (sortByField.toLowerCase()) {
            case "id":
                entityFieldName = "id";
                break;
            case "email":
                entityFieldName = "email";
                break;
            case "name":
                entityFieldName = "name";
                break;
            case "nickname":
                entityFieldName = "nickname";
                break;
            case "phone":
                entityFieldName = "phone";
                break;
            case "birth":
                entityFieldName = "birth";
                break;
            case "nationality":
                entityFieldName = "nationality.countryCode";
                break;
            case "isadmin":
                entityFieldName = "isAdmin";
                break;
            case "isgranted":
                entityFieldName = "isGranted";
                break;
            default:
                entityFieldName = "id"; // 기본값
                break;
        }
        
        // 정렬 및 페이징 설정
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<User> userPage;
        
        // searchBy에 따른 검색 로직
        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없으면 해당 조직의 모든 멤버 조회
            userPage = userRepository.findByOrganizationId(organizationId, pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    userPage = userRepository.findByOrganizationIdAndAllFieldsContaining(organizationId, search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.valueOf(search.trim());
                        userPage = userRepository.findByOrganizationIdAndId(organizationId, searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        userPage = Page.empty(pageable);
                    }
                    break;
                case "email":
                    userPage = userRepository.findByOrganizationIdAndEmailContainingIgnoreCase(organizationId, search.trim(), pageable);
                    break;
                case "name":
                    userPage = userRepository.findByOrganizationIdAndNameContainingIgnoreCase(organizationId, search.trim(), pageable);
                    break;
                case "nickname":
                    userPage = userRepository.findByOrganizationIdAndNicknameContainingIgnoreCase(organizationId, search.trim(), pageable);
                    break;
                case "phone":
                    userPage = userRepository.findByOrganizationIdAndPhoneContainingIgnoreCase(organizationId, search.trim(), pageable);
                    break;
                case "nationality":
                    userPage = userRepository.findByOrganizationIdAndNationality_CountryCodeContainingIgnoreCase(organizationId, search.trim(), pageable);
                    break;
                case "isadmin":
                    try {
                        Boolean isAdmin = Boolean.valueOf(search.trim().toLowerCase());
                        userPage = userRepository.findByOrganizationIdAndIsAdmin(organizationId, isAdmin, pageable);
                    } catch (Exception e) {
                        log.warn("Invalid boolean format for isAdmin search: {}", search);
                        userPage = Page.empty(pageable);
                    }
                    break;
                default:
                    // 기본적으로 전체 검색 수행
                    userPage = userRepository.findByOrganizationIdAndAllFieldsContaining(organizationId, search.trim(), pageable);
                    break;
            }
        }
        
        // Entity를 DTO로 변환하여 반환
        int totalPages = userPage.getTotalPages();
        long totalCount = userPage.getTotalElements();

        List<UserDTO> result = new ArrayList<>();

        for(User user : userPage.getContent()) {
            UserDTO userDTO = UserDTO.fromEntity(user);

            UserRole userRole = userRoleRepository.findById(UserRole.UserRoleId.builder().userId(user.getId()).orgId(organizationId).build()).get();
            
            userDTO.setIsGranted(userRole.getIsGranted());
            userDTO.setRole(userRole.getRole());
            result.add(userDTO);
        }
        
        log.info("Found {} organization members out of {} total", result.size(), userPage.getTotalElements());
        return new PagedResponse<UserDTO>(result, pageNum, pageSizeNum, totalCount, totalPages);
    }

    public void toggleUserRole(User user, Integer userId, Integer orgId) {
        UserRole.UserRoleId adminUserRoleId = UserRole.UserRoleId.builder()
            .userId(user.getId())
            .orgId(orgId)
            .build();

        UserRole adminUserRole = userRoleRepository.findById(adminUserRoleId)
            .orElseThrow(() -> new RuntimeException("Admin UserRole not found"));

        if(!user.getIsAdmin() && !adminUserRole.getRole().equals("admin")){
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

    public void changeUserRole(User user, Integer userId, Integer orgId, String newRole){
        
        UserRole.UserRoleId adminUserRoleId = UserRole.UserRoleId.builder()
            .userId(user.getId())
            .orgId(orgId)
            .build();

        UserRole adminUserRole = userRoleRepository.findById(adminUserRoleId)
            .orElseThrow(() -> new RuntimeException("Admin UserRole not found"));

        if(!user.getIsAdmin() && !adminUserRole.getRole().equals("admin")){
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
}
