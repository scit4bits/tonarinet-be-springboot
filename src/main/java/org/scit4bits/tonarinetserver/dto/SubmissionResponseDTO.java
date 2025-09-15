package org.scit4bits.tonarinetserver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Submission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 제출물 응답을 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Submission response DTO")
public class SubmissionResponseDTO {

    @Schema(description = "Submission ID", example = "1")
    private Integer id;

    @Schema(description = "Creation date and time", example = "2024-01-01T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Creator user ID", example = "1")
    private Integer createdById;

    @Schema(description = "Creator user information")
    private UserDTO createdBy;

    @Schema(description = "Submission contents", example = "This is my task submission with detailed work...")
    private String contents;

    @Schema(description = "Task ID", example = "1")
    private Integer taskId;

    @Schema(description = "Task information")
    private TaskResponseDTO task;

    @Schema(description = "File attachments")
    private List<FileAttachmentResponseDTO> fileAttachments;

    /**
     * Submission 엔티티를 SubmissionResponseDTO로 변환합니다.
     * @param submission 변환할 Submission 엔티티
     * @return 변환된 SubmissionResponseDTO 객체
     */
    public static SubmissionResponseDTO fromEntity(Submission submission) {
        SubmissionResponseDTOBuilder builder = SubmissionResponseDTO.builder()
                .id(submission.getId())
                .createdAt(submission.getCreatedAt())
                .createdById(submission.getCreatedById())
                .contents(submission.getContents())
                .taskId(submission.getTaskId());

        // 작성자 사용자 정보가 있는 경우 추가
        if (submission.getCreatedBy() != null) {
            builder.createdBy(UserDTO.fromEntity(submission.getCreatedBy()));
        }

        // 과제 정보가 있는 경우 추가
        if (submission.getTask() != null) {
            builder.task(TaskResponseDTO.fromEntity(submission.getTask()));
        }

        // 파일 첨부 정보가 있는 경우 추가
        if (submission.getFileAttachments() != null && !submission.getFileAttachments().isEmpty()) {
            builder.fileAttachments(submission.getFileAttachments().stream()
                    .map(FileAttachmentResponseDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}

