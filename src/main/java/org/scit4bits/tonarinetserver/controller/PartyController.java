package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.PartyRequestDTO;
import org.scit4bits.tonarinetserver.dto.PartyResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.PartyService;
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
@RequestMapping("/api/party")
@Tag(name = "Party", description = "Party management API")
public class PartyController {

    private final PartyService partyService;

    @PostMapping
    @Operation(summary = "Create a new party", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping
    @Operation(summary = "Get all parties")
    public ResponseEntity<List<PartyResponseDTO>> getAllParties() {
        try {
            List<PartyResponseDTO> parties = partyService.getAllParties();
            return ResponseEntity.ok(parties);
        } catch (Exception e) {
            log.error("Error fetching parties: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    @GetMapping("/{id}")
    @Operation(summary = "Get party by ID")
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

    @PutMapping("/{id}")
    @Operation(summary = "Update a party", security = @SecurityRequirement(name = "bearerAuth"))
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a party", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping("/search")
    @Operation(summary = "Search parties")
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

    @PostMapping("/{id}/join")
    @Operation(summary = "Join a party", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> joinParty(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            partyService.joinParty(id, user);
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

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave a party", security = @SecurityRequirement(name = "bearerAuth"))
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

    @PostMapping("/{id}/grant/{userId}")
    @Operation(summary = "Grant user access to party", security = @SecurityRequirement(name = "bearerAuth"))
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

    @PostMapping("/{id}/reject/{userId}")
    @Operation(summary = "Reject user access to party", security = @SecurityRequirement(name = "bearerAuth"))
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
