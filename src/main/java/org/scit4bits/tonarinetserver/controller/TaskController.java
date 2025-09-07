package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.TaskGroupResponseDTO;
import org.scit4bits.tonarinetserver.dto.TaskRequestDTO;
import org.scit4bits.tonarinetserver.dto.TaskResponseDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.TaskGroupService;
import org.scit4bits.tonarinetserver.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/task")
@Tag(name = "Task", description = "Task management API")
public class TaskController {

    private final TaskService taskService;
    private final TaskGroupService taskGroupService;

    @PostMapping
    @Operation(summary = "Create a new task", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> createTask(
            @Valid @RequestBody TaskRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            taskService.createTask(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("Task created successfully"));
        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all tasks", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only admin can see all tasks
        if (!user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            List<TaskResponseDTO> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error fetching tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            TaskResponseDTO task = taskService.getTaskById(id);
            // Check if user can access this task (creator, assignee, team member, or admin)
            // For now, allowing all authenticated users
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            log.error("Error fetching task: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching task: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my")
    @Operation(summary = "Get tasks assigned to current user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponseDTO>> getMyTasks(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            List<TaskResponseDTO> tasks = taskService.getTasksByUserId(user.getId());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error fetching user tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tasks assigned to specific user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUserId(
            @PathVariable Integer userId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only allow viewing own tasks or admin access
        if (!user.getId().equals(userId) && !user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            List<TaskResponseDTO> tasks = taskService.getTasksByUserId(userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error fetching user tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get tasks assigned to team", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponseDTO>> getTasksByTeamId(
            @PathVariable Integer teamId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Should check if user is member of the team
        try {
            List<TaskResponseDTO> tasks = taskService.getTasksByTeamId(teamId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error fetching team tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteTask(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            taskService.deleteTask(id, user);
            return ResponseEntity.ok(new SimpleResponse("Task deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the task creator") || 
                      e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting task: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/score")
    @Operation(summary = "Update task score", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponseDTO> updateTaskScore(
            @PathVariable Integer id,
            @RequestParam Integer score,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            TaskResponseDTO task = taskService.updateTaskScore(id, score, user);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the task creator") || 
                      e.getMessage().contains("admin") ||
                      e.getMessage().contains("Score must be")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating task score: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/taskgroup/{id}")
    @Operation(summary = "Get task group by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskGroupResponseDTO> getTaskGroupById(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            TaskGroupResponseDTO taskGroup = taskGroupService.getTaskGroupById(id);
            return ResponseEntity.ok(taskGroup);
        } catch (RuntimeException e) {
            log.error("Error fetching task group: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching task group: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search task groups within a specific organization", 
               description = "Search for task groups within the specified organization. All search operations are scoped to the provided organization.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PagedResponse<TaskGroupResponseDTO>> searchTasks(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "orgId", required = true) Integer orgId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (orgId == null) {
            log.error("Organization ID is required for task group searches");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            PagedResponse<TaskGroupResponseDTO> taskGroups = taskGroupService.searchTaskGroups(
                searchBy, search, orgId, page, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(taskGroups);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for task group search: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error searching task groups: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
