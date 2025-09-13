package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.TeamRequestDTO;
import org.scit4bits.tonarinetserver.dto.TeamResponseDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 팀 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/team")
@Tag(name = "Team", description = "팀 관리 API")
public class TeamController {

    private final TeamService teamService;

    /**
     * 새로운 팀을 생성합니다.
     * @param request 팀 생성 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 생성된 팀 정보
     */
    @PostMapping
    @Operation(summary = "새로운 팀 생성", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TeamResponseDTO> createTeam(
            @Valid @RequestBody TeamRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            TeamResponseDTO team = teamService.createTeam(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(team);
        } catch (Exception e) {
            log.error("Error creating team: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 모든 팀 목록을 조회합니다.
     * @return TeamResponseDTO 리스트
     */
    @GetMapping
    @Operation(summary = "모든 팀 조회")
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams() {
        try {
            List<TeamResponseDTO> teams = teamService.getAllTeams();
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error fetching teams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ID로 특정 팀 정보를 조회합니다.
     * @param id 조회할 팀 ID
     * @return TeamResponseDTO 형태의 팀 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 팀 조회")
    public ResponseEntity<TeamResponseDTO> getTeamById(@PathVariable("id") Integer id) {
        try {
            TeamResponseDTO team = teamService.getTeamById(id);
            return ResponseEntity.ok(team);
        } catch (RuntimeException e) {
            log.error("Error fetching team: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching team: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 로그인한 사용자가 속한 팀 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return TeamResponseDTO 리스트
     */
    @GetMapping("/my")
    public ResponseEntity<List<TeamResponseDTO>> getMyTeams(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<TeamResponseDTO> teams = teamService.getMyTeams(user);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error fetching user's teams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 조직에 속한 팀 목록을 조회합니다.
     * @param orgId 조직 ID
     * @return TeamResponseDTO 리스트
     */
    @GetMapping("/organization/{orgId}")
    @Operation(summary = "조직 ID로 팀 조회")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsByOrgId(@PathVariable("orgId") Integer orgId) {
        try {
            List<TeamResponseDTO> teams = teamService.getTeamsByOrgId(orgId);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error fetching teams by organization: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 팀 정보를 수정합니다.
     * @param id 수정할 팀 ID
     * @param request 팀 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 팀 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "팀 정보 수정", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TeamResponseDTO> updateTeam(
            @PathVariable("id") Integer id,
            @Valid @RequestBody TeamRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            TeamResponseDTO team = teamService.updateTeam(id, request, user);
            return ResponseEntity.ok(team);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the team leader") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating team: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 팀을 삭제합니다.
     * @param id 삭제할 팀 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "팀 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteTeam(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            teamService.deleteTeam(id, user);
            return ResponseEntity.ok(new SimpleResponse("Team deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the team leader") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting team: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 팀을 검색합니다.
     * @param searchBy 검색 기준 (all, name, description)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 TeamResponseDTO 리스트
     */
    @GetMapping("/search")
    @Operation(summary = "팀 검색")
    public ResponseEntity<PagedResponse<TeamResponseDTO>> searchTeams(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        try {
            PagedResponse<TeamResponseDTO> teams = teamService.searchTeams(
                    searchBy, search, page, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error searching teams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 팀에 참여합니다.
     * @param id 참여할 팀 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/{id}/join")
    @Operation(summary = "팀 참여", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> joinTeam(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            teamService.joinTeam(id, user);
            return ResponseEntity.ok(new SimpleResponse("Joined team successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error joining team: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 팀에서 나갑니다.
     * @param id 나갈 팀 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "팀 나가기", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> leaveTeam(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            teamService.leaveTeam(id, user);
            return ResponseEntity.ok(new SimpleResponse("Left team successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error leaving team: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
