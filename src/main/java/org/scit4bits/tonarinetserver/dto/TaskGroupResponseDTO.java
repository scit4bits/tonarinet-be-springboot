package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.scit4bits.tonarinetserver.entity.Task;

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
    private String name;
    private String contents;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
    private List<TaskResponseDTO> tasks;
    private Integer score;
    private Integer maxScore;


}
