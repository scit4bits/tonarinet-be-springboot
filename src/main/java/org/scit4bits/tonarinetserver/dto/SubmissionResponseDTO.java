package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.Submission;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static SubmissionResponseDTO fromEntity(Submission submission) {
        SubmissionResponseDTOBuilder builder = SubmissionResponseDTO.builder()
            .id(submission.getId())
            .createdAt(submission.getCreatedAt())
            .createdById(submission.getCreatedById())
            .contents(submission.getContents())
            .taskId(submission.getTaskId());

        // Add creator user if available
        if (submission.getCreatedBy() != null) {
            builder.createdBy(UserDTO.fromEntity(submission.getCreatedBy()));
        }

        // Add task if available
        if (submission.getTask() != null) {
            builder.task(TaskResponseDTO.fromEntity(submission.getTask()));
        }

        return builder.build();
    }
}
