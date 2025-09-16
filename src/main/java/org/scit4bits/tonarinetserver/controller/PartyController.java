package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.PartyJoinRequestDTO;
import org.scit4bits.tonarinetserver.dto.PartyRequestDTO;
import org.scit4bits.tonarinetserver.dto.PartyResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.PartyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 파티(모임) 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/party")
@Tag(name = "Party", description = "파티 관리 API")
public class PartyController {

    private final PartyService partyService;

    /**
     * 새로운 파티를 생성합니다.
     * @param request 파티 생성 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 생성된 파티 정보
     */
    @PostMapping
    @Operation(summary = "새로운 파티 생성", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PartyResponseDTO> createParty(
            @Valid @RequestBody PartyRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            PartyResponseDTO party = partyService.createParty(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(party);
        } catch (Exception e) {
            log.error("Error creating party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 모든 파티 목록을 조회합니다.
     * @return PartyResponseDTO 리스트
     */
    @GetMapping
    @Operation(summary = "모든 파티 조회")
    public ResponseEntity<List<PartyResponseDTO>> getAllParties() {
        try {
            List<PartyResponseDTO> parties = partyService.getAllParties();
            return ResponseEntity.ok(parties);
        } catch (Exception e) {
            log.error("Error fetching parties: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 로그인한 사용자가 속한 파티 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return PartyResponseDTO 리스트
     */
    @GetMapping("my")
    public ResponseEntity<List<PartyResponseDTO>> getMyParties(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<PartyResponseDTO> parties = partyService.getPartiesByUserId(user);
            return ResponseEntity.ok(parties);
        } catch (Exception e) {
            log.error("Error fetching user's parties: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ID로 특정 파티 정보를 조회합니다.
     * @param id 조회할 파티 ID
     * @return PartyResponseDTO 형태의 파티 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 파티 조회")
    public ResponseEntity<PartyResponseDTO> getPartyById(@PathVariable("id") Integer id) {
        try {
            PartyResponseDTO party = partyService.getPartyById(id);
            return ResponseEntity.ok(party);
        } catch (RuntimeException e) {
            log.error("Error fetching party: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 파티 정보를 수정합니다.
     * @param id 수정할 파티 ID
     * @param request 파티 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 파티 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "파티 정보 수정", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PartyResponseDTO> updateParty(
            @PathVariable("id") Integer id,
            @Valid @RequestBody PartyRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            PartyResponseDTO party = partyService.updateParty(id, request, user);
            return ResponseEntity.ok(party);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the party leader") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 파티를 삭제합니다.
     * @param id 삭제할 파티 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "파티 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteParty(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            partyService.deleteParty(id, user);
            return ResponseEntity.ok(new SimpleResponse("Party deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the party leader") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파티를 검색합니다.
     * @param searchBy 검색 기준 (all, name, description)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 PartyResponseDTO 리스트
     */
    @GetMapping("/search")
    @Operation(summary = "파티 검색")
    public ResponseEntity<PagedResponse<PartyResponseDTO>> searchParties(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        try {
            PagedResponse<PartyResponseDTO> parties = partyService.searchParties(
                    searchBy, search, page, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(parties);
        } catch (Exception e) {
            log.error("Error searching parties: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 파티에 참여합니다.
     * @param id 참여할 파티 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/{id}/join")
    @Operation(summary = "파티 참여", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> joinParty(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user,
            @RequestBody(required=false) PartyJoinRequestDTO request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            if(request == null) {
                request = new PartyJoinRequestDTO();
            }
            partyService.joinParty(id, user, request);
            return ResponseEntity.ok(new SimpleResponse("Joined party successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error joining party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 파티에서 나갑니다.
     * @param id 나갈 파티 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "파티 나가기", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> leaveParty(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            partyService.leaveParty(id, user);
            return ResponseEntity.ok(new SimpleResponse("Left party successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error leaving party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파티에 사용자를 초대(승인)합니다.
     * @param id 파티 ID
     * @param userId 초대할 사용자 ID
     * @param user 현재 로그인한 사용자 정보 (파티장)
     * @return 성공 응답
     */
    @PostMapping("/{id}/grant/{userId}")
    @Operation(summary = "파티에 사용자 초대(승인)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> grantUserForParty(
            @PathVariable("id") Integer id,
            @PathVariable("userId") Integer userId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            partyService.grantUserForParty(id, userId, user);
            return ResponseEntity.ok(new SimpleResponse("User access granted successfully"));
        } catch (RuntimeException e) {
            log.debug("Exception message: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the party leader") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error granting user access to party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파티 가입 신청을 거절합니다.
     * @param id 파티 ID
     * @param userId 거절할 사용자 ID
     * @param user 현재 로그인한 사용자 정보 (파티장)
     * @return 성공 응답
     */
    @PostMapping("/{id}/reject/{userId}")
    @Operation(summary = "파티 가입 신청 거절", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> rejectUserForParty(
            @PathVariable("id") Integer id,
            @PathVariable("userId") Integer userId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            partyService.rejectUserForParty(id, userId, user);
            return ResponseEntity.ok(new SimpleResponse("User access rejected successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the party leader") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error rejecting user access to party: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
