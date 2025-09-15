package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.ReplyRequestDTO;
import org.scit4bits.tonarinetserver.dto.ReplyResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ReplyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/reply")
@Tag(name = "Reply", description = "댓글 관리 API")
public class ReplyController {

    private final ReplyService replyService;

    /**
     * 새로운 댓글을 생성합니다.
     * @param request 댓글 생성 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 생성된 댓글 정보
     */
    @PostMapping
    @Operation(summary = "새로운 댓글 생성", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 모든 댓글 목록을 조회합니다. (관리자 전용)
     * @param user 현재 로그인한 사용자 정보
     * @return ReplyResponseDTO 리스트
     */
    @GetMapping
    @Operation(summary = "모든 댓글 조회 (관리자 전용)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ReplyResponseDTO>> getAllReplies(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 관리자만 모든 댓글을 조회할 수 있습니다.
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

    /**
     * ID로 특정 댓글을 조회합니다.
     * @param id 조회할 댓글 ID
     * @return ReplyResponseDTO 형태의 댓글 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 댓글 조회")
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

    /**
     * 특정 게시글의 모든 댓글을 조회합니다.
     * @param articleId 게시글 ID
     * @return ReplyResponseDTO 리스트
     */
    @GetMapping("/article/{articleId}")
    @Operation(summary = "게시글 ID로 댓글 조회")
    public ResponseEntity<List<ReplyResponseDTO>> getRepliesByArticleId(@PathVariable("articleId") Integer articleId) {
        try {
            List<ReplyResponseDTO> replies = replyService.getRepliesByArticleId(articleId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            log.error("Error fetching replies by article: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 댓글을 수정합니다.
     * @param id 수정할 댓글 ID
     * @param request 댓글 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 댓글 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "댓글 수정", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 특정 댓글을 삭제합니다.
     * @param id 삭제할 댓글 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "댓글 삭제", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 댓글을 검색합니다.
     * @param searchBy 검색 기준 (all, content, author)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 ReplyResponseDTO 리스트
     */
    @GetMapping("/search")
    @Operation(summary = "댓글 검색")
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
