package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 과제 생성 및 수정을 위한 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDTO {

    @NotBlank(message = "Task title is required")
    private String title;

    @NotBlank(message = "Task contents is required")
    private String contents;

    @NotNull(message = "Organization ID is required")
    private Integer orgId;

    private LocalDateTime dueDate;

    private List<Integer> assignedUserIds; // 특정 사용자에게 할당된 경우

    private List<Integer> assignedTeamIds; // 팀에 할당된 경우

    private Integer maxScore;
}
