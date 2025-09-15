package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.*;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.FileAttachmentService;
import org.scit4bits.tonarinetserver.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 과제 제출 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/submission")
@Tag(name = "Submission", description = "과제 제출 관리 API")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final FileAttachmentService fileAttachmentService;

    /**
     * 새로운 과제 제출을 생성합니다.
     * @param request 과제 제출 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 생성된 과제 제출 정보
     */
    @PostMapping
    @Operation(summary = "새로운 과제 제출 생성", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SubmissionResponseDTO> createSubmission(
            @Valid @RequestBody SubmissionRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            SubmissionResponseDTO submission = submissionService.createSubmission(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(submission);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일 첨부를 포함한 새로운 과제 제출을 생성합니다.
     * @param request 과제 제출 요청 정보
     * @param files 첨부 파일 리스트
     * @param user 현재 로그인한 사용자 정보
     * @return 생성된 과제 제출 정보
     */
    @PostMapping("/with-attachments")
    @Operation(summary = "파일 첨부를 포함한 새로운 과제 제출 생성", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SubmissionResponseDTO> createSubmissionWithAttachments(
            @RequestPart("request") @Valid SubmissionRequestDTO request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 먼저 과제 제출을 생성합니다.
            SubmissionResponseDTO submission = submissionService.createSubmission(request, user);

            // 파일이 제공된 경우, 업로드하고 과제 제출과 연결합니다.
            if (files != null && !files.isEmpty()) {
                FileAttachmentRequestDTO fileRequest = FileAttachmentRequestDTO.builder()
                        .submissionId(submission.getId())
                        .isPrivate(false) // 제출 첨부 파일은 기본적으로 비공개가 아닙니다.
                        .type(null) // 내용에 따라 파일 유형 자동 결정
                        .build();

                fileAttachmentService.uploadFiles(files, fileRequest, user);

                // 첨부 파일을 포함하기 위해 과제 제출 정보를 다시 가져옵니다.
                submission = submissionService.getSubmissionById(submission.getId());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(submission);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating submission with attachments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 모든 과제 제출 목록을 조회합니다. (관리자 전용)
     * @param user 현재 로그인한 사용자 정보
     * @return SubmissionResponseDTO 리스트
     */
    @GetMapping
    @Operation(summary = "모든 과제 제출 조회 (관리자 전용)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SubmissionResponseDTO>> getAllSubmissions(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 관리자만 모든 과제 제출을 볼 수 있습니다.
        if (!user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<SubmissionResponseDTO> submissions = submissionService.getAllSubmissions();
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("Error fetching submissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ID로 특정 과제 제출을 조회합니다.
     * @param id 조회할 과제 제출 ID
     * @param user 현재 로그인한 사용자 정보
     * @return SubmissionResponseDTO 형태의 과제 제출 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 과제 제출 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SubmissionResponseDTO> getSubmissionById(@PathVariable("id") Integer id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            SubmissionResponseDTO submission = submissionService.getSubmissionById(id);

            // 사용자는 자신의 제출물만 볼 수 있습니다 (관리자 제외).
            if (!submission.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(submission);
        } catch (RuntimeException e) {
            log.error("Error fetching submission: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 사용자의 모든 과제 제출을 조회합니다.
     * @param userId 사용자 ID
     * @param user 현재 로그인한 사용자 정보
     * @return SubmissionResponseDTO 리스트
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 ID로 과제 제출 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByUserId(
            @PathVariable("userId") Integer userId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 사용자는 자신의 제출물만 볼 수 있습니다 (관리자 제외).
        if (!user.getId().equals(userId) && !user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByUserId(userId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("Error fetching submissions by user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 로그인한 사용자의 모든 과제 제출을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return SubmissionResponseDTO 리스트
     */
    @GetMapping("/my")
    @Operation(summary = "현재 사용자의 과제 제출 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SubmissionResponseDTO>> getMySubmissions(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByUserId(user.getId());
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("Error fetching user's submissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 과제에 대한 모든 제출물을 조회합니다.
     * @param taskId 과제 ID
     * @param user 현재 로그인한 사용자 정보
     * @return SubmissionResponseDTO 리스트
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "과제 ID로 제출물 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByTaskId(
            @PathVariable("taskId") Integer taskId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByTaskId(taskId);

            // 관리자가 아닌 경우 자신의 제출물만 필터링합니다.
            if (!user.getIsAdmin()) {
                submissions = submissions.stream()
                        .filter(s -> s.getCreatedById().equals(user.getId()))
                        .toList();
            }

            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("Error fetching submissions by task: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 과제 제출을 수정합니다.
     * @param id 수정할 과제 제출 ID
     * @param request 과제 제출 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 과제 제출 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "과제 제출 수정", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SubmissionResponseDTO> updateSubmission(
            @PathVariable("id") Integer id,
            @Valid @RequestBody SubmissionRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            SubmissionResponseDTO submission = submissionService.updateSubmission(id, request, user);
            return ResponseEntity.ok(submission);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the submission creator") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 과제 제출을 삭제합니다.
     * @param id 삭제할 과제 제출 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "과제 제출 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteSubmission(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            submissionService.deleteSubmission(id, user);
            return ResponseEntity.ok(new SimpleResponse("Submission deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the submission creator") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 과제 제출을 검색합니다.
     * @param searchBy 검색 기준 (all, content)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @param user 현재 로그인한 사용자 정보
     * @return 페이징 처리된 SubmissionResponseDTO 리스트
     */
    @GetMapping("/search")
    @Operation(summary = "과제 제출 검색")
    public ResponseEntity<PagedResponse<SubmissionResponseDTO>> searchSubmissions(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal User user) {

        try {
            PagedResponse<SubmissionResponseDTO> submissions = submissionService.searchSubmissions(
                    searchBy, search, page, pageSize, sortBy, sortDirection);

            // 관리자가 아닌 경우 자신의 제출물만 필터링합니다.
            if (user != null && !user.getIsAdmin()) {
                List<SubmissionResponseDTO> filteredData = submissions.getData().stream()
                        .filter(s -> s.getCreatedById().equals(user.getId()))
                        .toList();
                submissions.setData(filteredData);
            }

            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            log.error("Error searching submissions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 과제 제출의 첨부 파일을 조회합니다.
     * @param id 과제 제출 ID
     * @param user 현재 로그인한 사용자 정보
     * @return FileAttachmentResponseDTO 리스트
     */
    @GetMapping("/{id}/attachments")
    @Operation(summary = "과제 제출의 첨부 파일 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<FileAttachmentResponseDTO>> getSubmissionAttachments(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 먼저 사용자가 이 제출물에 접근할 수 있는지 확인합니다.
            SubmissionResponseDTO submission = submissionService.getSubmissionById(id);

            // 사용자는 자신의 제출물에 대한 첨부 파일만 볼 수 있습니다 (관리자 제외).
            if (!submission.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<FileAttachmentResponseDTO> attachments = fileAttachmentService.getFileAttachmentsBySubmissionId(id, user);
            return ResponseEntity.ok(attachments);
        } catch (RuntimeException e) {
            log.error("Error fetching submission attachments: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error fetching submission attachments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
