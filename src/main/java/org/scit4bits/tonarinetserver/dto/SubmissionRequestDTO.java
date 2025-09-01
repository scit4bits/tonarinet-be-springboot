package org.scit4bits.tonarinetserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Submission request DTO")
public class SubmissionRequestDTO {

    @Schema(description = "Task ID for the submission", example = "1")
    @NotNull(message = "Task ID is required")
    private Integer taskId;

    @Schema(description = "Submission contents", example = "This is my task submission with detailed work...")
    @NotBlank(message = "Contents is required")
    private String contents;
}
