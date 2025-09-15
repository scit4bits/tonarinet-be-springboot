package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 파일 첨부 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Tag(name = "FileAttachment", description = "파일 첨부 관리 API")
public class FileAttachmentController {

    private final FileAttachmentService fileAttachmentService;

    /**
     * 파일을 업로드합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param files 업로드할 파일 리스트
     * @param requestDTO 파일 메타데이터
     * @return 업로드된 파일 정보 리스트
     */
    @PostMapping("/upload")
    @Operation(summary = "파일 업로드", description = "파일을 업로드하고, 선택적으로 게시글에 연결합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "파일 업로드 성공", content = @Content(schema = @Schema(implementation = FileAttachmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 또는 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "해당 게시글에 파일을 첨부할 수 없음")
    })
    public ResponseEntity<List<FileAttachmentResponseDTO>> uploadFile(
            @AuthenticationPrincipal User user,
            @RequestPart(name = "files") List<MultipartFile> files,
            @RequestPart("metadata") FileAttachmentRequestDTO requestDTO) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 파일 업로드 서비스를 호출합니다.
            List<FileAttachmentResponseDTO> result = fileAttachmentService.uploadFiles(files, requestDTO, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.debug("Failed to upload multiple files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일을 다운로드합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param id 다운로드할 파일 ID
     * @return 파일 데이터
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "파일 다운로드", description = "파일 첨부파일을 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "비공개 파일에 접근할 수 없음"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    public ResponseEntity<ByteArrayResource> downloadFile(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer id) {

        try {
            FileAttachmentResponseDTO fileInfo = fileAttachmentService.getFileAttachmentById(id, user);

            // 첨부파일 타입이고, 로그인하지 않은 사용자는 다운로드할 수 없습니다.
            if (fileInfo.getType() == FileType.ATTACHMENT && user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            byte[] fileContent = fileAttachmentService.downloadFile(id, user);

            ByteArrayResource resource = new ByteArrayResource(fileContent);

            // 캐시 검증을 위해 파일 ID와 마지막 수정 시간을 기반으로 ETag를 생성합니다.
            String etag = "\"" + fileInfo.getId() + "-"
                    + (fileInfo.getUploadedAt() != null ? fileInfo.getUploadedAt().hashCode() : fileContent.hashCode())
                    + "\"";

            switch (fileInfo.getType()) {
                case ATTACHMENT:
                    // 파일 이름에 ASCII가 아닌 문자가 포함된 경우를 처리합니다.
                    String asciiFallback = fileInfo.getOriginalFilename().replaceAll("[^\\x20-\\x7E]", "_");
                    String filenameStar = "UTF-8''"
                            + URLEncoder.encode(fileInfo.getOriginalFilename(), StandardCharsets.UTF_8);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + asciiFallback + "\"; filename*=" + filenameStar)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                            .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600") // 1시간 동안 캐시
                            .header(HttpHeaders.ETAG, etag)
                            .body(resource);
                case IMAGE:
                    // 이미지의 경우 더 길게 캐시할 수 있습니다.
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                            .header(HttpHeaders.CACHE_CONTROL, "private, max-age=86400") // 24시간 동안 캐시
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

    /**
     * 특정 제출물에 첨부된 모든 파일을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param submissionId 조회할 제출물 ID
     * @return 파일 정보 리스트
     */
    @GetMapping("/submission/{submissionId}")
    @Operation(summary = "제출물 ID로 파일 조회", description = "특정 제출물에 첨부된 모든 파일을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "제출물을 찾을 수 없음")
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
