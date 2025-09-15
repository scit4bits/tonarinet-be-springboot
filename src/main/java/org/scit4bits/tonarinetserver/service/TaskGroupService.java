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

/**
 * 과제 그룹 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TaskGroupService {

    private final TaskGroupRepository taskGroupRepository;

    /**
     * ID로 특정 과제 그룹을 조회합니다.
     * @param id 조회할 과제 그룹 ID
     * @return TaskGroupResponseDTO
     */
    @Transactional(readOnly = true)
    public TaskGroupResponseDTO getTaskGroupById(Integer id) {
        log.info("ID로 과제 그룹 조회: {}", id);
        TaskGroup taskGroup = taskGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("과제 그룹을 찾을 수 없습니다. ID: " + id));
        return TaskGroupResponseDTO.fromEntity(taskGroup);
    }

    /**
     * 특정 조직 내에서 과제 그룹을 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param orgId 조직 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 TaskGroupResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskGroupResponseDTO> searchTaskGroups(String searchBy, String search, Integer orgId,
                                                                Integer page, Integer pageSize, String sortBy, String sortDirection) {
        log.info("조직 {} 내에서 과제 그룹 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
                orgId, searchBy, search, page, pageSize, sortBy, sortDirection);

        // 조직 ID는 필수입니다.
        if (orgId == null) {
            log.error("모든 과제 그룹 검색에는 조직 ID가 필요합니다.");
            throw new IllegalArgumentException("모든 과제 그룹 작업에는 조직 ID가 필요합니다.");
        }

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "title" -> "title";
            case "created" -> "createdAt";
            case "due" -> "dueDate";
            case "maxscore" -> "maxScore";
            default -> "id";
        };

        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<TaskGroup> taskGroupPage;

        if (search == null || search.trim().isEmpty()) {
            // 검색어가 없으면 해당 조직의 모든 과제 그룹 조회
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
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
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
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    taskGroupPage = taskGroupRepository.findByOrgIdAndAllFieldsContaining(orgId, search.trim(), pageable);
                    break;
            }
        }

        List<TaskGroupResponseDTO> result = taskGroupPage.getContent().stream()
                .map(TaskGroupResponseDTO::fromEntity)
                .toList();

        log.info("총 {}개의 과제 그룹 중 {}개를 찾았습니다.", taskGroupPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, taskGroupPage.getTotalElements(), taskGroupPage.getTotalPages());
    }
}
