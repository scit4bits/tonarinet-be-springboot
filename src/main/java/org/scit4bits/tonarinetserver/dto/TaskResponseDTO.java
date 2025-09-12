package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static TaskResponseDTO fromEntity(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .contents(task.getContents())
                .createdById(task.getCreatedById())
                .createdByName(task.getCreatedBy() != null ? task.getCreatedBy().getName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .dueDate(task.getDueDate())
                .userId(task.getUserId())
                .assignedUserName(task.getUser() != null ? task.getUser().getName() : null)
                .teamId(task.getTeamId())
                .teamName(task.getTeam() != null ? task.getTeam().getName() : null)
                .score(task.getScore())
                .maxScore(task.getMaxScore())
                .taskGroupId(task.getTaskGroupId())
                .build();
    }
}
