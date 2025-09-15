package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.*;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.AIService;
import org.scit4bits.tonarinetserver.service.TaskGroupService;
import org.scit4bits.tonarinetserver.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 과제 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/task")
@Tag(name = "Task", description = "과제 관리 API")
public class TaskController {

    private final TaskService taskService;
    private final TaskGroupService taskGroupService;
    private final AIService aiService;

    /**
     * 새로운 과제를 생성합니다.
     * @param request 과제 생성 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping
    @Operation(summary = "새로운 과제 생성", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 모든 과제 목록을 조회합니다. (관리자 전용)
     * @param user 현재 로그인한 사용자 정보
     * @return TaskResponseDTO 리스트
     */
    @GetMapping
    @Operation(summary = "모든 과제 조회 (관리자 전용)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 관리자만 모든 과제를 조회할 수 있습니다.
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

    /**
     * ID로 특정 과제를 조회합니다.
     * @param id 조회할 과제 ID
     * @param user 현재 로그인한 사용자 정보
     * @return TaskResponseDTO 형태의 과제 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 과제 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            TaskResponseDTO task = taskService.getTaskById(id);
            // TODO: 사용자가 이 과제에 접근할 수 있는지 확인 (생성자, 담당자, 팀 멤버, 관리자)
            // 현재는 모든 인증된 사용자를 허용합니다.
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            log.error("Error fetching task: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching task: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 로그인한 사용자에게 할당된 과제 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return TaskResponseDTO 리스트
     */
    @GetMapping("/my")
    @Operation(summary = "현재 사용자에게 할당된 과제 조회", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 특정 사용자에게 할당된 과제 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param user 현재 로그인한 사용자 정보
     * @return TaskResponseDTO 리스트
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "특정 사용자에게 할당된 과제 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUserId(
            @PathVariable("userId") Integer userId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 자신의 과제 또는 관리자만 조회를 허용합니다.
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

    /**
     * 특정 팀에 할당된 과제 목록을 조회합니다.
     * @param teamId 팀 ID
     * @param user 현재 로그인한 사용자 정보
     * @return TaskResponseDTO 리스트
     */
    @GetMapping("/team/{teamId}")
    @Operation(summary = "팀에 할당된 과제 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponseDTO>> getTasksByTeamId(
            @PathVariable("teamId") Integer teamId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // TODO: 사용자가 팀의 멤버인지 확인해야 합니다.
        try {
            List<TaskResponseDTO> tasks = taskService.getTasksByTeamId(teamId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error fetching team tasks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 과제를 삭제합니다.
     * @param id 삭제할 과제 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "과제 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteTask(
            @PathVariable("id") Integer id,
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

    /**
     * 과제 점수를 수정합니다.
     * @param id 과제 ID
     * @param request 점수 및 피드백 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 과제 정보
     */
    @PatchMapping("/{id}/score")
    @Operation(summary = "과제 점수 수정", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponseDTO> updateTaskScore(
            @PathVariable("id") Integer id,
            @RequestBody TaskScoreUpdateRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            TaskResponseDTO task = taskService.updateTaskScore(id, request.getScore(), request.getFeedback(), user);
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

    /**
     * ID로 과제 그룹을 조회합니다.
     * @param id 과제 그룹 ID
     * @param user 현재 로그인한 사용자 정보
     * @return TaskGroupResponseDTO 형태의 과제 그룹 정보
     */
    @GetMapping("/taskgroup/{id}")
    @Operation(summary = "ID로 과제 그룹 조회", security = @SecurityRequirement(name = "bearerAuth"))
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

    /**
     * 특정 조직 내에서 과제 그룹을 검색합니다.
     * @param searchBy 검색 기준 (all, name, description)
     * @param search 검색어
     * @param orgId 조직 ID (필수)
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @param user 현재 로그인한 사용자 정보
     * @return 페이징 처리된 TaskGroupResponseDTO 리스트
     */
    @GetMapping("/search")
    @Operation(summary = "특정 조직 내 과제 그룹 검색", description = "지정된 조직 내에서 과제 그룹을 검색합니다. 모든 검색 작업은 제공된 조직으로 범위가 지정됩니다.", security = @SecurityRequirement(name = "bearerAuth"))
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
            log.error("과제 그룹 검색에는 조직 ID가 필요합니다.");
            return ResponseEntity.badRequest().build();
        }

        try {
            PagedResponse<TaskGroupResponseDTO> taskGroups = taskGroupService.searchTaskGroups(
                    searchBy, search, orgId, page, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(taskGroups);
        } catch (IllegalArgumentException e) {
            log.error("과제 그룹 검색에 대한 잘못된 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("과제 그룹 검색 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 과제에 대한 관리 권한이 있는지 확인합니다.
     * @param id 과제 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 권한 여부
     */
    @GetMapping("/{id}/canmgmt")
    public ResponseEntity<Boolean> getPrivilegeOfTask(@PathVariable("id") String id,
                                                      @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean hasPrivilege = taskService.checkTaskPrivilege(user, id);
        return ResponseEntity.ok(hasPrivilege);
    }

    /**
     * AI를 사용하여 과제 추천을 생성합니다.
     * @param entity 프롬프트 정보
     * @return HTML 형식의 AI 응답
     */
    @PostMapping("/ai-recommend")
    public ResponseEntity<String> postTaskAIRecommend(@RequestBody TaskAIRecommendRequestDTO entity) {
        if (entity.getPrompt() == null || entity.getPrompt().isEmpty()) {
            return ResponseEntity.badRequest().body("프롬프트는 비워둘 수 없습니다.");
        }

        String htmlResponse = aiService.generateHTMLResponse(entity.getPrompt());

        return ResponseEntity.ok(htmlResponse);
    }

}
