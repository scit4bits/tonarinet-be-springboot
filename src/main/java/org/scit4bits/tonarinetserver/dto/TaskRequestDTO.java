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

    @NotBlank(message = "Task title is required")
    private String title;
    
    @NotBlank(message = "Task contents is required")
    private String contents;
    
    @NotNull(message = "Organization ID is required")
    private Integer orgId;
    
    private LocalDateTime dueDate;
    
    private List<Integer> assignedUserIds; // If assigned to specific user

    private List<Integer> assignedTeamIds; // If assigned to team

    private Integer maxScore;
}
