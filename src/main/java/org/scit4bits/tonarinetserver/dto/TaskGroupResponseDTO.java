package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.TaskGroup;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 과제 그룹 응답을 위한 DTO
 */
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

    /**
     * TaskGroup 엔티티를 TaskGroupResponseDTO로 변환합니다.
     * @param taskGroup 변환할 TaskGroup 엔티티
     * @return 변환된 TaskGroupResponseDTO 객체
     */
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
