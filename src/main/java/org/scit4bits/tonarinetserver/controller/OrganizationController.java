package org.scit4bits.tonarinetserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.OrganizationDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/organization")
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping("/create")
    public ResponseEntity<OrganizationDTO> createOrganization(@RequestBody OrganizationDTO organizationDTO) {
        Organization createdOrganization = organizationService.createOrganization(organizationDTO);
        return ResponseEntity.ok(OrganizationDTO.fromEntity(createdOrganization));
    }
    

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<OrganizationDTO>> getOrganizationSearch(
        @RequestParam(name="searchBy", defaultValue = "all") String searchBy,
        @RequestParam(name="search", defaultValue = "") String search,
        @RequestParam(name="page", defaultValue = "0") Integer page,
        @RequestParam(name="pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(name="sortBy", defaultValue = "id") String sortBy,
        @RequestParam(name="sortDirection", defaultValue = "asc") String sortDirection
    ){
        PagedResponse<OrganizationDTO> organizations = organizationService.searchOrganization(searchBy, search, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(organizations);
    }

    @PostMapping("/apply")
    public ResponseEntity<SimpleResponse> postOrganizationApply(
            @RequestParam("organizationId") Integer organizationId,
            @RequestParam(name="entryMessage", defaultValue = "") String entryMessage,
            @AuthenticationPrincipal User user) {
        try {
            organizationService.applyToOrganization(user, organizationId, entryMessage);
            return ResponseEntity.ok(new SimpleResponse("조직 가입 신청이 완료되었습니다."));
        } catch (Exception e) {
            log.error("Error applying to organization: ", e);
            return ResponseEntity.badRequest().body(new SimpleResponse("조직 가입 신청 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

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
