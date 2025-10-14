package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Task;

import java.time.LocalDateTime;

/**
 * 과제 응답을 위한 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponseDTO {
    private Integer id;
    private String name;
    private String contents;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
    private Integer userId;
    private String assignedUserName;
    private Integer teamId;
    private String teamName;
    private Integer score;
    private Integer maxScore;
    private String feedback;
    private Integer taskGroupId;

    /**
     * Task 엔티티를 TaskResponseDTO로 변환합니다.
     * @param task 변환할 Task 엔티티
     * @return 변환된 TaskResponseDTO 객체
     */
    public static TaskResponseDTO fromEntity(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .contents(task.getContents())
                .createdById(task.getCreatedById())
                .createdByName(task.getCreatedBy() != null ? task.getCreatedBy().getNickname() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .dueDate(task.getDueDate())
                .userId(task.getUserId())
                .assignedUserName(task.getUser() != null ? task.getUser().getName() : null)
                .teamId(task.getTeamId())
                .teamName(task.getTeam() != null ? task.getTeam().getName() : null)
                .feedback(task.getFeedback())
                .score(task.getScore())
                .maxScore(task.getMaxScore())
                .taskGroupId(task.getTaskGroupId())
                .build();
    }
}
