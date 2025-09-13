package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TaskGroupResponseDTO;
import org.scit4bits.tonarinetserver.entity.TaskGroup;
import org.scit4bits.tonarinetserver.repository.TaskGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TaskGroupService {

    private final TaskGroupRepository taskGroupRepository;

    @Transactional(readOnly = true)
    public TaskGroupResponseDTO getTaskGroupById(Integer id) {
        log.info("Fetching task group with id: {}", id);
        TaskGroup taskGroup = taskGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaskGroup not found with id: " + id));
        return TaskGroupResponseDTO.fromEntity(taskGroup);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskGroupResponseDTO> searchTaskGroups(String searchBy, String search, Integer orgId,
                                                                Integer page, Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching task groups within organization {} with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
                orgId, searchBy, search, page, pageSize, sortBy, sortDirection);

        // Validate required organization ID
        if (orgId == null) {
            log.error("Organization ID is required for all TaskGroup searches");
            throw new IllegalArgumentException("Organization ID is required for all TaskGroup operations");
        }

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
            case "title":
                entityFieldName = "title";
                break;
            case "created":
                entityFieldName = "createdAt";
                break;
            case "due":
                entityFieldName = "dueDate";
                break;
            case "maxscore":
                entityFieldName = "maxScore";
                break;
            default:
                entityFieldName = "id";
                break;
        }

        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<TaskGroup> taskGroupPage;

        if (search == null || search.trim().isEmpty()) {
            // If no search term, get all TaskGroups for the organization
            taskGroupPage = taskGroupRepository.findByOrgId(orgId, pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    taskGroupPage = taskGroupRepository.findByOrgIdAndAllFieldsContaining(orgId, search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        taskGroupPage = taskGroupRepository.findByOrgIdAndId(orgId, searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        taskGroupPage = Page.empty(pageable);
                    }
                    break;
                case "title":
                    taskGroupPage = taskGroupRepository.findByOrgIdAndTitleContainingIgnoreCase(orgId, search.trim(), pageable);
                    break;
                case "contents":
                    taskGroupPage = taskGroupRepository.findByOrgIdAndContentsContainingIgnoreCase(orgId, search.trim(), pageable);
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    taskGroupPage = taskGroupRepository.findByOrgIdAndAllFieldsContaining(orgId, search.trim(), pageable);
                    break;
            }
        }

        List<TaskGroupResponseDTO> result = taskGroupPage.getContent().stream()
                .map(TaskGroupResponseDTO::fromEntity)
                .toList();

        log.info("Found {} task groups out of {} total", result.size(), taskGroupPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, taskGroupPage.getTotalElements(), taskGroupPage.getTotalPages());
    }
}
