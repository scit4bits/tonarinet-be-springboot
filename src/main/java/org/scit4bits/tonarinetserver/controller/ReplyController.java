package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.ReplyRequestDTO;
import org.scit4bits.tonarinetserver.dto.ReplyResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ReplyService;
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
@RequestMapping("/api/reply")
@Tag(name = "Reply", description = "Reply management API")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping
    @Operation(summary = "Create a new reply", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReplyResponseDTO> createReply(
            @Valid @RequestBody ReplyRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ReplyResponseDTO reply = replyService.createReply(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(reply);
        } catch (Exception e) {
            log.error("Error creating reply: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all replies", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ReplyResponseDTO>> getAllReplies(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only admin can see all replies
        if (!user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            List<ReplyResponseDTO> replies = replyService.getAllReplies();
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            log.error("Error fetching replies: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reply by ID")
    public ResponseEntity<ReplyResponseDTO> getReplyById(@PathVariable("id") Integer id) {
        try {
            ReplyResponseDTO reply = replyService.getReplyById(id);
            return ResponseEntity.ok(reply);
        } catch (RuntimeException e) {
            log.error("Error fetching reply: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching reply: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/article/{articleId}")
    @Operation(summary = "Get replies by article ID")
    public ResponseEntity<List<ReplyResponseDTO>> getRepliesByArticleId(@PathVariable("articleId") Integer articleId) {
        try {
            List<ReplyResponseDTO> replies = replyService.getRepliesByArticleId(articleId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            log.error("Error fetching replies by article: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reply", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReplyResponseDTO> updateReply(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ReplyRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ReplyResponseDTO reply = replyService.updateReply(id, request, user);
            return ResponseEntity.ok(reply);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the reply creator") || 
                      e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating reply: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reply", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteReply(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            replyService.deleteReply(id, user);
            return ResponseEntity.ok(new SimpleResponse("Reply deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the reply creator") || 
                      e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting reply: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search replies")
    public ResponseEntity<PagedResponse<ReplyResponseDTO>> searchReplies(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        try {
            PagedResponse<ReplyResponseDTO> replies = replyService.searchReplies(
                searchBy, search, page, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            log.error("Error searching replies: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
