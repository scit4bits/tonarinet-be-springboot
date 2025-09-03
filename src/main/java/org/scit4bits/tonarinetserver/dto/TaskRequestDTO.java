package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;
import java.util.List;

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
    
    private List<UserDTO> assignedUsers; // If assigned to specific user
    
    private List<TeamResponseDTO> assignedTeams; // If assigned to team
    
    private Integer maxScore;
}
