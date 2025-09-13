package org.scit4bits.tonarinetserver.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.FileAttachmentRequestDTO;
import org.scit4bits.tonarinetserver.dto.FileAttachmentResponseDTO;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.FileAttachmentService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Tag(name = "FileAttachment", description = "File attachment management API")
public class FileAttachmentController {

    private final FileAttachmentService fileAttachmentService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Upload a file with optional article association")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully", content = @Content(schema = @Schema(implementation = FileAttachmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot attach to this article")
    })
    public ResponseEntity<List<FileAttachmentResponseDTO>> uploadFile(
            @AuthenticationPrincipal User user,
            @RequestPart(name = "files") List<MultipartFile> files,
            @RequestPart("metadata") FileAttachmentRequestDTO requestDTO) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            try {
                List<FileAttachmentResponseDTO> result = fileAttachmentService.uploadFiles(files, requestDTO, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            } catch (Exception e) {
                log.debug("Failed to upload multiple files: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (RuntimeException e) {
            log.error("Failed to upload file for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download file", description = "Download a file attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot access private file"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<ByteArrayResource> downloadFile(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer id) {

        try {
            FileAttachmentResponseDTO fileInfo = fileAttachmentService.getFileAttachmentById(id, user);

            if (fileInfo.getType() == FileType.ATTACHMENT && user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            byte[] fileContent = fileAttachmentService.downloadFile(id, user);

            ByteArrayResource resource = new ByteArrayResource(fileContent);

            // Generate ETag based on file ID and last modified time for cache validation
            String etag = "\"" + fileInfo.getId() + "-"
                    + (fileInfo.getUploadedAt() != null ? fileInfo.getUploadedAt().hashCode() : fileContent.hashCode())
                    + "\"";

            switch (fileInfo.getType()) {
                case ATTACHMENT:
                    String asciiFallback = fileInfo.getOriginalFilename().replaceAll("[^\\x20-\\x7E]", "_");
                    String filenameStar = "UTF-8''"
                            + URLEncoder.encode(fileInfo.getOriginalFilename(), StandardCharsets.UTF_8);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + asciiFallback + "\"; filename*=" + filenameStar)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                            .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600") // Cache for 1 hour
                            .header(HttpHeaders.ETAG, etag)
                            .body(resource);
                case IMAGE:
                    // Handle image case - images can be cached longer
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                            .header(HttpHeaders.CACHE_CONTROL, "private, max-age=86400") // Cache for 24 hours
                            .header(HttpHeaders.ETAG, etag)
                            .body(resource);
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + fileInfo.getType());
            }

        } catch (RuntimeException e) {
            log.error("Failed to download file {} for user {}: {}", id, user != null ? user.getId() : "anonymous",
                    e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/submission/{submissionId}")
    @Operation(summary = "Get files by submission ID", description = "Get all file attachments for a specific submission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Submission not found")
    })
    public ResponseEntity<List<FileAttachmentResponseDTO>> getFilesBySubmissionId(
            @AuthenticationPrincipal User user,
            @PathVariable("submissionId") Integer submissionId) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<FileAttachmentResponseDTO> files = fileAttachmentService.getFileAttachmentsBySubmissionId(submissionId,
                    user);
            return ResponseEntity.ok(files);
        } catch (RuntimeException e) {
            log.error("Failed to get files for submission {} for user {}: {}", submissionId, user.getId(),
                    e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
