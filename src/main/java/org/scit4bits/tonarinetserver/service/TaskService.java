package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TaskRequestDTO;
import org.scit4bits.tonarinetserver.dto.TaskResponseDTO;
import org.scit4bits.tonarinetserver.entity.*;
import org.scit4bits.tonarinetserver.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 과제 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
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
    private final UserTeamRepository userTeamRepository;

    /**
     * 새로운 과제를 생성합니다. 과제 그룹을 먼저 생성하고, 각 담당자/팀에 대한 개별 과제를 생성합니다.
     * @param request 과제 생성 요청 정보
     * @param creator 과제 생성자 정보
     */
    public void createTask(TaskRequestDTO request, User creator) {
        log.info("과제 생성 - 이름: {}, 생성자: {}, 조직: {}", request.getTitle(), creator.getId(), request.getOrgId());

        // 과제 그룹 생성
        TaskGroup taskGroup = TaskGroup.builder()
                .title(request.getTitle())
                .contents(request.getContents())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore())
                .orgId(request.getOrgId())
                .build();

        TaskGroup savedTaskGroup = taskGroupRepository.save(taskGroup);

        // 개별 사용자에게 과제 할당
        for (Integer userId : request.getAssignedUserIds()) {
            User dbUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));
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

        // 팀에 과제 할당
        for (Integer teamId : request.getAssignedTeamIds()) {
            Team dbTeam = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다. ID: " + teamId));

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

    /**
     * 모든 과제 목록을 조회합니다.
     * @return TaskResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {
        log.info("모든 과제 조회");
        return taskRepository.findAll().stream()
                .map(TaskResponseDTO::fromEntity)
                .toList();
    }

    /**
     * ID로 특정 과제를 조회합니다.
     * @param id 조회할 과제 ID
     * @return TaskResponseDTO
     */
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Integer id) {
        log.info("ID로 과제 조회: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + id));
        return TaskResponseDTO.fromEntity(task);
    }

    /**
     * 특정 사용자에게 할당된 모든 과제를 조회합니다. (개인 할당 및 팀 할당 포함)
     * @param userId 사용자 ID
     * @return TaskResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUserId(Integer userId) {
        log.info("사용자 {}의 과제 조회", userId);

        // 사용자에게 직접 할당된 과제 조회
        List<Task> userTasks = taskRepository.findByUserIdOrderByDueDateAsc(userId);

        // 사용자가 속한 팀 ID 목록 조회
        List<UserTeam> userTeams = userTeamRepository.findByIdUserId(userId);
        List<Integer> teamIds = userTeams.stream()
                .map(userTeam -> userTeam.getId().getTeamId())
                .toList();

        // 해당 팀에 할당된 과제 조회
        List<Task> teamTasks = new ArrayList<>();
        for (Integer teamId : teamIds) {
            teamTasks.addAll(taskRepository.findByTeamIdOrderByDueDateAsc(teamId));
        }

        // 두 목록을 합치고 중복 제거
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(userTasks);
        allTasks.addAll(teamTasks);

        // DTO로 변환하고 마감일 순으로 정렬
        return allTasks.stream()
                .distinct()
                .sorted((t1, t2) -> {
                    if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                    if (t1.getDueDate() == null) return 1;
                    if (t2.getDueDate() == null) return -1;
                    return t1.getDueDate().compareTo(t2.getDueDate());
                })
                .map(TaskResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 특정 팀에 할당된 모든 과제를 조회합니다.
     * @param teamId 팀 ID
     * @return TaskResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByTeamId(Integer teamId) {
        log.info("팀 {}의 과제 조회", teamId);
        return taskRepository.findByTeamIdOrderByDueDateAsc(teamId).stream()
                .map(TaskResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 과제를 삭제합니다.
     * @param id 삭제할 과제 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void deleteTask(Integer id, User user) {
        log.info("과제 삭제 - ID: {}, 사용자: {}", id, user.getId());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + id));

        // 사용자가 생성자이거나 관리자인지 확인
        if (!task.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("과제 생성자 또는 관리자만 삭제할 수 있습니다.");
        }

        taskRepository.deleteById(id);
        log.info("과제 삭제 완료");
    }

    /**
     * 과제 점수를 수정하고 사용자에게 알림을 보냅니다.
     * @param id 과제 ID
     * @param score 점수
     * @param feedback 피드백
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 과제 정보
     */
    public TaskResponseDTO updateTaskScore(Integer id, Integer score, String feedback, User user) {
        log.info("과제 점수 수정 - ID: {}, 점수: {}, 사용자: {}", id, score, user.getId());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + id));

        // 사용자가 생성자이거나 관리자인지 확인 (채점자 역할도 확인 가능)
        if (!task.getCreatedById().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("과제 생성자 또는 관리자만 점수를 수정할 수 있습니다.");
        }

        if (score < 0 || (task.getMaxScore() != null && score > task.getMaxScore())) {
            throw new RuntimeException("점수는 0과 만점 사이여야 합니다.");
        }

        task.setScore(score);
        task.setFeedback(feedback);
        Task savedTask = taskRepository.save(task);
        log.info("과제 점수 수정 완료");

        // 사용자에게 알림 발송
        notificationService.addNotification(task.getUserId(), "{\"messageType\": \"taskScoreUpdated\", \"taskTitle\": \"" + task.getName() + "\"}", "/task/" + task.getId());

        return TaskResponseDTO.fromEntity(savedTask);
    }

    /**
     * 과제를 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 TaskResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponseDTO> searchTasks(String searchBy, String search, Integer page,
                                                      Integer pageSize, String sortBy, String sortDirection) {
        log.info("과제 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "name" -> "name";
            case "created" -> "createdAt";
            case "due" -> "dueDate";
            case "score" -> "score";
            default -> "id";
        };

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
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
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
                        log.warn("잘못된 생성자 ID 형식으로 검색: {}", search);
                        taskPage = Page.empty(pageable);
                    }
                    break;
                case "assignee":
                    try {
                        Integer assigneeId = Integer.parseInt(search.trim());
                        taskPage = taskRepository.findByUserId(assigneeId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 담당자 ID 형식으로 검색: {}", search);
                        taskPage = Page.empty(pageable);
                    }
                    break;
                case "team":
                    try {
                        Integer teamId = Integer.parseInt(search.trim());
                        taskPage = taskRepository.findByTeamId(teamId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 팀 ID 형식으로 검색: {}", search);
                        taskPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    taskPage = taskRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<TaskResponseDTO> result = taskPage.getContent().stream()
                .map(TaskResponseDTO::fromEntity)
                .toList();

        log.info("총 {}개의 과제 중 {}개를 찾았습니다.", taskPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, taskPage.getTotalElements(), taskPage.getTotalPages());
    }

    /**
     * 사용자가 특정 과제에 대한 관리 권한이 있는지 확인합니다.
     * @param user 사용자 정보
     * @param taskId 과제 ID
     * @return 권한 여부
     */
    public boolean checkTaskPrivilege(User user, String taskId) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + user.getId()));
        if (dbUser.getIsAdmin()) {
            return true;
        }
        Task task = taskRepository.findById(Integer.parseInt(taskId))
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + taskId));

        // 생성자인지 확인
        if (task.getCreatedById().equals(dbUser.getId())) {
            return true;
        }

        Integer targetOrgId = task.getTaskGroup().getOrgId();

        // 해당 조직의 관리자인지 확인
        return dbUser.getUserRoles().stream().anyMatch(role -> role.getId().getOrgId().equals(targetOrgId) && role.getRole().equals("admin"));
    }
}
