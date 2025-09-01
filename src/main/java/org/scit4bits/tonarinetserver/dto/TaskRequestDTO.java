package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDTO {
    
    @NotBlank(message = "Task name is required")
    private String name;
    
    @NotBlank(message = "Task contents is required")
    private String contents;
    
    private LocalDateTime dueDate;
    
    private Integer userId; // If assigned to specific user
    
    private Integer teamId; // If assigned to team
    
    private Integer maxScore;
    
    @NotNull(message = "Task group ID is required")
    private Integer taskGroupId;
}
