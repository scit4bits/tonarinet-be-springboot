package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.FileAttachmentRequestDTO;
import org.scit4bits.tonarinetserver.dto.FileAttachmentResponseDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.SubmissionRequestDTO;
import org.scit4bits.tonarinetserver.dto.SubmissionResponseDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.FileAttachmentService;
import org.scit4bits.tonarinetserver.service.SubmissionService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/submission")
@Tag(name = "Submission", description = "Submission management API")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final FileAttachmentService fileAttachmentService;

    @PostMapping
    @Operation(summary = "Create a new submission", security = @SecurityRequirement(name = "bearerAuth"))
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

    @PostMapping("/with-attachments")
    @Operation(summary = "Create a new submission with file attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SubmissionResponseDTO> createSubmissionWithAttachments(
            @RequestPart("request") @Valid SubmissionRequestDTO request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // First create the submission
            SubmissionResponseDTO submission = submissionService.createSubmission(request, user);
            
            // If files are provided, upload them and associate with the submission
            if (files != null && !files.isEmpty()) {
                FileAttachmentRequestDTO fileRequest = FileAttachmentRequestDTO.builder()
                    .submissionId(submission.getId())
                    .isPrivate(false) // Default to not private for submission attachments
                    .type(null) // Auto-determine file type based on content
                    .build();
                
                fileAttachmentService.uploadFiles(files, fileRequest, user);
                
                // Re-fetch the submission to include the attachments
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

    @GetMapping
    @Operation(summary = "Get all submissions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SubmissionResponseDTO>> getAllSubmissions(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only admin can see all submissions
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

    @GetMapping("/{id}")
    @Operation(summary = "Get submission by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SubmissionResponseDTO> getSubmissionById(@PathVariable("id") Integer id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            SubmissionResponseDTO submission = submissionService.getSubmissionById(id);
            
            // Users can only see their own submissions unless they're admin
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

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get submissions by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByUserId(
            @PathVariable("userId") Integer userId, 
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Users can only see their own submissions unless they're admin
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

    @GetMapping("/my")
    @Operation(summary = "Get current user's submissions", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get submissions by task ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByTaskId(
            @PathVariable("taskId") Integer taskId, 
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only admin or task creator can see all submissions for a task
        // Regular users can only see their own submissions (handled in service)
        
        try {
            List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByTaskId(taskId);
            
            // Filter submissions to only show user's own unless admin
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

    @PutMapping("/{id}")
    @Operation(summary = "Update a submission", security = @SecurityRequirement(name = "bearerAuth"))
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a submission", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping("/search")
    @Operation(summary = "Search submissions")
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
            
            // Filter results for non-admin users to only show their own submissions
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

    @GetMapping("/{id}/attachments")
    @Operation(summary = "Get file attachments for a submission", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<FileAttachmentResponseDTO>> getSubmissionAttachments(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // First check if user can access this submission
            SubmissionResponseDTO submission = submissionService.getSubmissionById(id);
            
            // Users can only see attachments for their own submissions unless they're admin
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
