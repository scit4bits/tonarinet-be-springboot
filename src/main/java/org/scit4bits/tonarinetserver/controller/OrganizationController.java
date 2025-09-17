package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.OrganizationDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 조직 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/organization")
public class OrganizationController {
    private final OrganizationService organizationService;

    /**
     * 새로운 조직을 생성합니다.
     * @param organizationDTO 생성할 조직 정보
     * @return 생성된 조직 정보
     */
    @PostMapping("/create")
    public ResponseEntity<OrganizationDTO> createOrganization(@RequestBody OrganizationDTO organizationDTO) {
        Organization createdOrganization = organizationService.createOrganization(organizationDTO);
        return ResponseEntity.ok(OrganizationDTO.fromEntity(createdOrganization));
    }

    /**
     * 조직 상세 정보를 조회합니다.
     * @param orgId 조직 ID
     * @return OrganizationDTO 객체 (존재하지 않으면 null)
     */
    @GetMapping("/{orgId}")
    public ResponseEntity<OrganizationDTO> getOrganizationDetail(@PathVariable Integer orgId) {
        OrganizationDTO organization = organizationService.getOrganizationDetail(orgId);
        if (organization == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(organization);
    }

    /**
     * 현재 로그인한 사용자가 속한 조직 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return OrganizationDTO 리스트
     */
    @GetMapping("/my")
    public ResponseEntity<List<OrganizationDTO>> getMyOrganizations(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<OrganizationDTO> organizations = organizationService.getMyOrganizations(user);
        return ResponseEntity.ok(organizations);
    }

    /**
     * 조직을 검색합니다.
     * @param searchBy 검색 기준 (all, name, description)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 OrganizationDTO 리스트
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<OrganizationDTO>> getOrganizationSearch(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection
    ) {
        PagedResponse<OrganizationDTO> organizations = organizationService.searchOrganization(searchBy, search, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(organizations);
    }

    /**
     * 조직에 가입을 신청합니다.
     * @param organizationId 가입할 조직 ID
     * @param entryMessage 가입 메시지
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/apply")
    public ResponseEntity<SimpleResponse> postOrganizationApply(
            @RequestParam("organizationId") Integer organizationId,
            @RequestParam(name = "entryMessage", defaultValue = "") String entryMessage,
            @AuthenticationPrincipal User user) {
        try {
            organizationService.applyToOrganization(user, organizationId, entryMessage);
            return ResponseEntity.ok(new SimpleResponse("조직 가입 신청이 완료되었습니다."));
        } catch (Exception e) {
            log.error("Error applying to organization: ", e);
            return ResponseEntity.badRequest().body(new SimpleResponse("조직 가입 신청 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 조직 정보를 업데이트합니다.
     * @param organizationDTO 업데이트할 조직 정보
     * @return 성공 응답
     */
    @PostMapping("/update")
    public ResponseEntity<SimpleResponse> postOrganizationUpdate(@RequestBody OrganizationDTO organizationDTO) {
        try {
            organizationService.updateOrganization(organizationDTO);
            return ResponseEntity.ok(new SimpleResponse("조직 정보가 업데이트되었습니다."));
        } catch (Exception e) {
            log.error("Error updating organization: ", e);
            return ResponseEntity.badRequest().body(new SimpleResponse("조직 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
