package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.OrganizationDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.*;
import org.scit4bits.tonarinetserver.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 조직 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleService userRoleService;
    private final BoardRepository boardRepository;

    /**
     * 새로운 조직을 생성하고, 해당 조직의 기본 게시판을 함께 생성합니다.
     * @param request 조직 생성 요청 정보
     * @return 생성된 조직 엔티티
     */
    public Organization createOrganization(OrganizationDTO request) {
        Country country = countryRepository.findById(request.getCountryCode()).get();
        Organization organization = Organization.builder()
                .name(request.getName())
                .country(country)
                .type(request.getType())
                .description(request.getDescription())
                .build();

        Organization savedOrganization = organizationRepository.save(organization);

        // 조직 생성 시 기본 게시판 생성
        Board newBoard = Board.builder()
                .title(request.getName().concat(" 게시판"))
                .organization(savedOrganization)
                .build();

        boardRepository.save(newBoard);

        return savedOrganization;
    }

    /**
     * 모든 조직 목록을 조회합니다.
     * @return OrganizationDTO 리스트
     */
    public List<OrganizationDTO> getAllOrganizations() {
        log.info("모든 조직 정보 조회");
        List<Organization> entities = organizationRepository.findAll();
        return entities.stream()
                .map(OrganizationDTO::fromEntity)
                .toList();
    }

    /**
     * 조직을 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 OrganizationDTO
     */
    public PagedResponse<OrganizationDTO> searchOrganization(String searchBy, String search, Integer page,
                                                             Integer pageSize, String sortBy, String sortDirection) {
        log.info(
                "조직 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "name" -> "name";
            case "country" -> "countryCode";
            case "type" -> "type";
            default -> "id"; // 기본값
        };

        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<Organization> organizationPage;

        if (search == null || search.trim().isEmpty()) {
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
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
                        organizationPage = Page.empty(pageable);
                    }
                    break;
                case "name":
                    organizationPage = organizationRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "country":
                    organizationPage = organizationRepository.findByCountryCodeContainingIgnoreCase(search.trim(),
                            pageable);
                    break;
                case "type":
                    organizationPage = organizationRepository.findByTypeContainingIgnoreCase(search.trim(), pageable);
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    organizationPage = organizationRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<OrganizationDTO> result = organizationPage.getContent().stream()
                .map(OrganizationDTO::fromEntity)
                .toList();

        log.info("총 {}개의 조직 중 {}개를 찾았습니다.", organizationPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, organizationPage.getTotalElements(), organizationPage.getTotalPages());
    }

    /**
     * 조직에 가입을 신청합니다.
     * @param user 가입을 신청하는 사용자
     * @param organizationId 가입할 조직 ID
     * @param entryMessage 가입 메시지
     */
    public void applyToOrganization(User user, Integer organizationId, String entryMessage) {

        Organization organization = organizationRepository.findById(organizationId).get();
        log.info("사용자 {}가 조직 {}에 가입을 신청합니다.", user.getEmail(), organization.getName());

        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(user.getId())
                .orgId(organizationId)
                .build();

        // 이미 멤버인지 확인
        if (userRoleRepository.existsById(userRoleId)) {
            log.warn("사용자 {}는 이미 조직 {}의 멤버입니다.", user.getEmail(), organization.getName());
            throw new IllegalStateException("이미 조직의 멤버입니다.");
        }

        // UserRole 엔티티 생성 및 저장
        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(user)
                .role("member") // 기본 역할은 'member'
                .organization(organization)
                .isGranted(false) // 초기에는 승인되지 않은 상태
                .entryMessage(entryMessage)
                .build();

        userRoleRepository.save(userRole);
        log.info("사용자 {}가 조직 {}에 성공적으로 가입 신청했습니다.", user.getEmail(), organization.getName());
    }

    /**
     * 조직 가입 신청을 승인합니다. (관리자 권한 필요)
     * @param adminUser 관리자 사용자 정보
     * @param targetUserId 대상 사용자 ID
     * @param organizationId 조직 ID
     */
    public void approveOrganizationMembership(User adminUser, Integer targetUserId, Integer organizationId) {
        Organization organization = organizationRepository.findById(organizationId).get();
        // 관리자 권한 확인
        if (!userRoleService.checkUsersRoleInOrg(adminUser, organization, "admin")) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }

        User targetUser = userRepository.findById(targetUserId).get();

        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(targetUser.getId())
                .orgId(organization.getId())
                .build();

        UserRole userRole = userRoleRepository.findById(userRoleId).get();

        userRole.setIsGranted(true);
        userRoleRepository.save(userRole);

        log.info("사용자 {}의 조직 {} 멤버십을 승인했습니다.", targetUser.getId(), organization.getId());
    }

    /**
     * 조직에서 사용자를 제거합니다. (관리자 권한 필요)
     * @param adminUser 관리자 사용자 정보
     * @param targetUserId 대상 사용자 ID
     * @param organizationId 조직 ID
     */
    public void removeUserFromOrganization(User adminUser, Integer targetUserId, Integer organizationId) {
        Organization organization = organizationRepository.findById(organizationId).get();
        // 관리자 권한 확인
        if (!userRoleService.checkUsersRoleInOrg(adminUser, organization, "admin")) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }

        User targetUser = userRepository.findById(targetUserId).get();

        UserRole.UserRoleId userRoleId = UserRole.UserRoleId.builder()
                .userId(targetUser.getId())
                .orgId(organization.getId())
                .build();

        userRoleRepository.deleteById(userRoleId);
        log.info("조직 {}에서 사용자 {}를 제거했습니다.", organization.getId(), targetUser.getId());
    }

    /**
     * 조직 정보를 수정합니다.
     * @param organizationDTO 수정할 조직 정보
     */
    public void updateOrganization(OrganizationDTO organizationDTO) {
        Organization organization = organizationRepository.findById(organizationDTO.getId()).get();

        organization.setName(organizationDTO.getName() != null && !organizationDTO.getName().trim().isEmpty()
                ? organizationDTO.getName()
                : organization.getName());
        organization.setDescription(
                organizationDTO.getDescription() != null && !organizationDTO.getDescription().trim().isEmpty()
                        ? organizationDTO.getDescription()
                        : organization.getDescription());
        organization.setCountryCode(
                organizationDTO.getCountryCode() != null && !organizationDTO.getCountryCode().trim().isEmpty()
                        ? organizationDTO.getCountryCode()
                        : organization.getCountryCode());
        organization.setType(organizationDTO.getType() != null && !organizationDTO.getType().trim().isEmpty()
                ? organizationDTO.getType()
                : organization.getType());
        organizationRepository.save(organization);
    }

    /**
     * 조직을 삭제합니다.
     * @param id 삭제할 조직 ID
     */
    public void deleteOrganization(Integer id) {
        log.info("ID {}의 조직을 삭제합니다.", id);
        organizationRepository.deleteById(id);
    }

    /**
     * 특정 사용자가 속한 조직 목록과 해당 조직에서의 역할을 조회합니다.
     * @param user 사용자 정보
     * @return 역할 정보가 포함된 OrganizationDTO 리스트
     */
    public List<OrganizationDTO> getMyOrganizations(User user) {
        User dbUser = userRepository.findById(user.getId()).get();
        List<Organization> organizations = dbUser.getOrganizations();

        return organizations.stream()
                .map(org -> {
                    String role = userRoleRepository.findById(UserRole.UserRoleId.builder()
                                    .userId(user.getId())
                                    .orgId(org.getId())
                                    .build())
                            .get().getRole();
                    OrganizationDTO dto = OrganizationDTO.fromEntity(org);
                    dto.setRole(role);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
