package org.scit4bits.tonarinetserver.service;

import java.util.ArrayList;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TaskRequestDTO;
import org.scit4bits.tonarinetserver.dto.TaskResponseDTO;
import org.scit4bits.tonarinetserver.dto.TeamResponseDTO;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.entity.Task;
import org.scit4bits.tonarinetserver.entity.TaskGroup;
import org.scit4bits.tonarinetserver.entity.Team;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.TaskGroupRepository;
import org.scit4bits.tonarinetserver.repository.TaskRepository;
import org.scit4bits.tonarinetserver.repository.TeamRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TaskService {
    
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final TaskGroupRepository taskGroupRepository;    
    private final NotificationService notificationService;

    public void createTask(TaskRequestDTO request, User creator) {
        log.info("Creating task with name: {} by user: {} for organization: {}", request.getTitle(), creator.getId(), request.getOrgId());

        TaskGroup taskGroup = TaskGroup.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore())
                .orgId(request.getOrgId())
                .build();

        TaskGroup savedTaskGroup = taskGroupRepository.save(taskGroup);

        for(Integer userId : request.getAssignedUserIds()){
            User dbUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            Task task = Task.builder()
            .name(savedTaskGroup.getTitle())
            .contents(savedTaskGroup.getContents())
            .createdById(creator.getId())
            .maxScore(savedTaskGroup.getMaxScore())
            .taskGroupId(savedTaskGroup.getId())
            .userId(dbUser.getId())
            .dueDate(savedTaskGroup.getDueDate())
            .build();

            taskRepository.save(task);
        }

        for(Integer teamId : request.getAssignedTeamIds()){
            Team dbTeam = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));

            Task task = Task.builder()
                .name(savedTaskGroup.getTitle())
                .contents(savedTaskGroup.getContents())
                .createdById(creator.getId())
                .maxScore(savedTaskGroup.getMaxScore())
                .taskGroupId(savedTaskGroup.getId())
                .teamId(dbTeam.getId())
                .dueDate(savedTaskGroup.getDueDate())
                .build();

            taskRepository.save(task);
        }
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {
        log.info("Fetching all tasks");
        return taskRepository.findAll().stream()
                .map(TaskResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Integer id) {
        log.info("Fetching task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return TaskResponseDTO.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUserId(Integer userId) {
        log.info("Fetching tasks for user: {}", userId);
        return taskRepository.findByUserIdOrderByDueDateAsc(userId).stream()
                .map(TaskResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByTeamId(Integer teamId) {
        log.info("Fetching tasks for team: {}", teamId);
        return taskRepository.findByTeamIdOrderByDueDateAsc(teamId).stream()
                .map(TaskResponseDTO::fromEntity)
                .toList();
    }


    public void deleteTask(Integer id, User user) {
        log.info("Deleting task with id: {} by user: {}", id, user.getId());
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        // Check if user is the creator or admin
        if (!task.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the task creator or admin can delete the task");
        }
        
        taskRepository.deleteById(id);
        log.info("Task deleted successfully");
    }

    public TaskResponseDTO updateTaskScore(Integer id, Integer score, User user) {
        log.info("Updating task score for task id: {} to {} by user: {}", id, score, user.getId());
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        // Check if user is the creator or admin (could also check for grader role)
        if (!task.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the task creator or admin can update the task score");
        }
        
        if (score < 0 || (task.getMaxScore() != null && score > task.getMaxScore())) {
            throw new RuntimeException("Score must be between 0 and max score");
        }
        
        task.setScore(score);
        Task savedTask = taskRepository.save(task);
        log.info("Task score updated successfully");

        // send notification to the user
        notificationService.addNotification(task.getUserId(), "Task score updated", "/task/" + task.getId());

        return TaskResponseDTO.fromEntity(savedTask);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponseDTO> searchTasks(String searchBy, String search, Integer page, 
                                                     Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching tasks with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}", 
                searchBy, search, page, pageSize, sortBy, sortDirection);
        
        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";
        
        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // sortBy 필드명 매핑
        String entityFieldName;
        switch (sortByField.toLowerCase()) {
            case "id":
                entityFieldName = "id";
                break;
            case "name":
                entityFieldName = "name";
                break;
            case "created":
                entityFieldName = "createdAt";
                break;
            case "due":
                entityFieldName = "dueDate";
                break;
            case "score":
                entityFieldName = "score";
                break;
            default:
                entityFieldName = "id";
                break;
        }
        
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<Task> taskPage;
        
        if (search == null || search.trim().isEmpty()) {
            taskPage = taskRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    taskPage = taskRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        taskPage = taskRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        taskPage = Page.empty(pageable);
                    }
                    break;
                case "name":
                    taskPage = taskRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "contents":
                    taskPage = taskRepository.findByContentsContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "creator":
                    try {
                        Integer creatorId = Integer.parseInt(search.trim());
                        taskPage = taskRepository.findByCreatedById(creatorId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid creator ID format for search: {}", search);
                        taskPage = Page.empty(pageable);
                    }
                    break;
                case "assignee":
                    try {
                        Integer assigneeId = Integer.parseInt(search.trim());
                        taskPage = taskRepository.findByUserId(assigneeId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid assignee ID format for search: {}", search);
                        taskPage = Page.empty(pageable);
                    }
                    break;
                case "team":
                    try {
                        Integer teamId = Integer.parseInt(search.trim());
                        taskPage = taskRepository.findByTeamId(teamId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid team ID format for search: {}", search);
                        taskPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    taskPage = taskRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }
        
        List<TaskResponseDTO> result = taskPage.getContent().stream()
                .map(TaskResponseDTO::fromEntity)
                .toList();
        
        log.info("Found {} tasks out of {} total", result.size(), taskPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, taskPage.getTotalElements(), taskPage.getTotalPages());
    }

    public boolean checkTaskPrivilege(User user, String taskId) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));
        if (dbUser.getIsAdmin()) {
            return true;
        }
        Task task = taskRepository.findById(Integer.parseInt(taskId))
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        // Check if user is the creator
        if (task.getCreatedById().equals(dbUser.getId())) {
            return true;
        }

        
        Integer targetOrgId = task.getTaskGroup().getOrgId();

        if (dbUser.getUserRoles().stream().anyMatch(role -> role.getId().getOrgId().equals(targetOrgId) && role.getRole().equals("admin"))) {
            return true;
        }

        return false;
    }
}
