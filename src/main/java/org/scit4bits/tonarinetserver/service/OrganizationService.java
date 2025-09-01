package org.scit4bits.tonarinetserver.service;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.OrganizationDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserRole;
import org.scit4bits.tonarinetserver.repository.OrganizationRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.repository.UserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleService userRoleService;

    public Organization createOrganization(OrganizationDTO request) {
        Organization organization = Organization.builder()
                .name(request.getName())
                .countryCode(request.getCountryCode())
                .type(request.getType())
                .description(request.getDescription())
                .build();
        return organizationRepository.save(organization);
    }

    public List<OrganizationDTO> getAllOrganizations() {
        log.info("Fetching all organizations");
        List<Organization> entities = organizationRepository.findAll();
        return entities.stream()
                .map(OrganizationDTO::fromEntity)
                .toList();
    }

    public PagedResponse<OrganizationDTO> searchOrganization(String searchBy, String search, Integer page, Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching organizations with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}", 
                searchBy, search, page, pageSize, sortBy, sortDirection);
        
        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "all";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";
        
        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // sortBy 필드명 매핑 (엔티티 필드명과 일치하도록)
        String entityFieldName;
        switch (sortByField.toLowerCase()) {
            case "id":
                entityFieldName = "id";
                break;
            case "name":
                entityFieldName = "name";
                break;
            case "country":
                entityFieldName = "countryCode";
                break;
            case "type":
                entityFieldName = "type";
                break;
            default:
                entityFieldName = "id"; // 기본값
                break;
        }
        
        // 정렬 및 페이징 설정
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<Organization> organizationPage;
        
        // searchBy에 따른 검색 로직
        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없으면 모든 조직 조회
            organizationPage = organizationRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    organizationPage = organizationRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        organizationPage = organizationRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        organizationPage = Page.empty(pageable);
                    }
                    break;
                case "name":
                    organizationPage = organizationRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "country":
                    organizationPage = organizationRepository.findByCountryCodeContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "type":
                    organizationPage = organizationRepository.findByTypeContainingIgnoreCase(search.trim(), pageable);
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    organizationPage = organizationRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }
        
        // Entity를 DTO로 변환하여 반환
        int totalPages = organizationPage.getTotalPages();
        //Integer totalEntities = organizationPage.
        long totalCount = organizationPage.getTotalElements();


        List<OrganizationDTO> result = organizationPage.getContent().stream()
                .map(OrganizationDTO::fromEntity)
                .toList();
        
        log.info("Found {} organizations out of {} total", result.size(), organizationPage.getTotalElements());
        return new PagedResponse<OrganizationDTO>(result, pageNum, pageSizeNum, totalCount, totalPages);
    }

    public void applyToOrganization(User user, Integer organizationId, String entryMessage) {
        
        Organization organization = organizationRepository.findById(organizationId).get();
        log.info("User {} is applying to organization with id: {}", user.getEmail(), organization.getName());
        
        // 중간자 엔티티를 사용한 올바른 방법
        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(user.getId())
                .orgId(organizationId)
                .build();
        
        // 이미 존재하는지 확인
        if (userRoleRepository.existsById(userRoleId)) {
            log.warn("User {} is already a member of organization {}", user.getEmail(), organization.getName());
            throw new IllegalStateException("User is already a member of organization");
        }
        
        // UserRole 엔티티 생성 및 저장
        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(user)
                .role("member") // 기본 역할 설정
                .organization(organization)
                .isGranted(false) // 초기에는 승인되지 않은 상태
                .entryMessage(entryMessage) // 기본 메시지
                .build();
        
        // 중간자 테이블에 직접 저장 - 이것만으로 충분!
        userRoleRepository.save(userRole);
        log.info("User {} successfully applied to organization {}", user.getEmail(), organization.getName());
    }

    /**
     * 조직 가입 신청을 승인하는 메서드
     */
    public void approveOrganizationMembership(User adminUser, Integer targetUserId, Integer organizationId) {
        Organization organization = organizationRepository.findById(organizationId).get();
        User user = userRepository.findById(targetUserId).get();
        if(!userRoleService.checkAdminPrivileges(user, organization)){
            throw new AccessDeniedException("Admin privileges required");
        }

        User targetUser = userRepository.findById(targetUserId).get();

        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(targetUser.getId())
                .orgId(organization.getId())
                .build();
        
        UserRole userRole = userRoleRepository.findById(userRoleId).get();
        
        userRole.setIsGranted(true);
        userRoleRepository.save(userRole);

        log.info("Approved user {} membership to organization {}", targetUser.getId(), organization.getId());
    }

    /**
     * 조직에서 사용자를 제거하는 메서드
     */
    public void removeUserFromOrganization(User adminUser, Integer targetUserId, Integer organizationId) {
        Organization organization = organizationRepository.findById(organizationId).get();
        if (!userRoleService.checkAdminPrivileges(adminUser, organization)) {
            throw new AccessDeniedException("Admin privileges required");
        }

        User targetUser = userRepository.findById(targetUserId).get();

        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(targetUser.getId())
                .orgId(organization.getId())
                .build();
        
        userRoleRepository.deleteById(userRoleId);
        log.info("Removed user {} from organization {}", targetUser.getId(), organization.getId());
    }

    public void updateOrganization(OrganizationDTO organizationDTO) {
        Organization organization = organizationRepository.findById(organizationDTO.getId()).get();

        organization.setName(organizationDTO.getName()!=null && !organizationDTO.getName().trim().isEmpty() ? organizationDTO.getName(): organization.getName());
        organization.setDescription(organizationDTO.getDescription() != null && !organizationDTO.getDescription().trim().isEmpty() ? organizationDTO.getDescription() : organization.getDescription());
        organization.setCountryCode(organizationDTO.getCountryCode() != null && !organizationDTO.getCountryCode().trim().isEmpty() ? organizationDTO.getCountryCode() : organization.getCountryCode());
        organization.setType(organizationDTO.getType() != null && !organizationDTO.getType().trim().isEmpty() ? organizationDTO.getType() : organization.getType());
        organizationRepository.save(organization);
    }

    public void deleteOrganization(Integer id) {
        log.info("Deleting organization with id: {}", id);
        organizationRepository.deleteById(id);
    }
}
