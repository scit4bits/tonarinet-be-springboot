package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.TeamRequestDTO;
import org.scit4bits.tonarinetserver.dto.TeamResponseDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/team")
@Tag(name = "Team", description = "Team management API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "Create a new team", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping
    @Operation(summary = "Get all teams")
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams() {
        try {
            List<TeamResponseDTO> teams = teamService.getAllTeams();
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error fetching teams: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
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

    @GetMapping("/organization/{orgId}")
    @Operation(summary = "Get teams by organization ID")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsByOrgId(@PathVariable("orgId") Integer orgId) {
        try {
            List<TeamResponseDTO> teams = teamService.getTeamsByOrgId(orgId);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            log.error("Error fetching teams by organization: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a team", security = @SecurityRequirement(name = "bearerAuth"))
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping("/search")
    @Operation(summary = "Search teams")
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

    @PostMapping("/{id}/join")
    @Operation(summary = "Join a team", security = @SecurityRequirement(name = "bearerAuth"))
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

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave a team", security = @SecurityRequirement(name = "bearerAuth"))
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
