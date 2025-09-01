package org.scit4bits.tonarinetserver.service;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TaskRequestDTO;
import org.scit4bits.tonarinetserver.dto.TaskResponseDTO;
import org.scit4bits.tonarinetserver.entity.Task;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.TaskRepository;
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
    
    private final TaskRepository taskRepository;

    public TaskResponseDTO createTask(TaskRequestDTO request, User creator) {
        log.info("Creating task with name: {} by user: {}", request.getName(), creator.getId());
        
        Task task = Task.builder()
                .name(request.getName())
                .contents(request.getContents())
                .createdById(creator.getId())
                .dueDate(request.getDueDate())
                .userId(request.getUserId())
                .teamId(request.getTeamId())
                .maxScore(request.getMaxScore())
                .taskGroupId(request.getTaskGroupId())
                .score(0) // Initial score
                .build();
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());
        return TaskResponseDTO.fromEntity(savedTask);
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

    public TaskResponseDTO updateTask(Integer id, TaskRequestDTO request, User user) {
        log.info("Updating task with id: {} by user: {}", id, user.getId());
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        // Check if user is the creator or admin
        if (!task.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the task creator or admin can update the task");
        }
        
        task.setName(request.getName() != null && !request.getName().trim().isEmpty() 
                    ? request.getName() : task.getName());
        task.setContents(request.getContents() != null && !request.getContents().trim().isEmpty() 
                        ? request.getContents() : task.getContents());
        task.setDueDate(request.getDueDate() != null ? request.getDueDate() : task.getDueDate());
        task.setUserId(request.getUserId() != null ? request.getUserId() : task.getUserId());
        task.setTeamId(request.getTeamId() != null ? request.getTeamId() : task.getTeamId());
        task.setMaxScore(request.getMaxScore() != null ? request.getMaxScore() : task.getMaxScore());
        task.setTaskGroupId(request.getTaskGroupId() != null ? request.getTaskGroupId() : task.getTaskGroupId());
        
        Task savedTask = taskRepository.save(task);
        log.info("Task updated successfully");
        return TaskResponseDTO.fromEntity(savedTask);
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
}
