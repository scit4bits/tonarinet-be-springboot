package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.scit4bits.tonarinetserver.entity.TaskGroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskGroupResponseDTO {
    private Integer id;
    private String title;
    private String contents;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private Integer maxScore;
    private Integer orgId;
    private String organizationName;
    private List<TaskResponseDTO> tasks;

    public static TaskGroupResponseDTO fromEntity(TaskGroup taskGroup) {
        return TaskGroupResponseDTO.builder()
                .id(taskGroup.getId())
                .title(taskGroup.getTitle())
                .contents(taskGroup.getContents())
                .createdAt(taskGroup.getCreatedAt())
                .dueDate(taskGroup.getDueDate())
                .maxScore(taskGroup.getMaxScore())
                .orgId(taskGroup.getOrgId())
                .organizationName(taskGroup.getOrganization() != null ? taskGroup.getOrganization().getName() : null)
                .tasks(taskGroup.getTasks() != null ? 
                    taskGroup.getTasks().stream().map(TaskResponseDTO::fromEntity).toList() : null)
                .build();
    }
}
