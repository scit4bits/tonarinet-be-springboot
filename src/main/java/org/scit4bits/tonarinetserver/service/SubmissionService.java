package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SubmissionRequestDTO;
import org.scit4bits.tonarinetserver.dto.SubmissionResponseDTO;
import org.scit4bits.tonarinetserver.entity.Submission;
import org.scit4bits.tonarinetserver.entity.Task;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.SubmissionRepository;
import org.scit4bits.tonarinetserver.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 과제 제출 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;

    /**
     * 새로운 과제 제출을 생성합니다.
     * @param requestDTO 제출 요청 정보
     * @param currentUser 현재 로그인한 사용자 정보
     * @return 생성된 제출 정보
     */
    public SubmissionResponseDTO createSubmission(SubmissionRequestDTO requestDTO, User currentUser) {
        // 과제가 존재하는지 확인
        Task task = taskRepository.findById(requestDTO.getTaskId())
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + requestDTO.getTaskId()));

        // 사용자가 이 과제에 제출할 수 있는지 확인 (담당자 또는 팀 멤버)
        boolean canSubmit = task.getUserId() != null && task.getUserId().equals(currentUser.getId());

        // 사용자가 할당된 팀의 멤버인 경우
        if (task.getTeamId() != null && task.getTeam() != null) {
            boolean isTeamMember = task.getTeam().getUsers().stream()
                    .anyMatch(user -> user.getId().equals(currentUser.getId()));
            if (isTeamMember) {
                canSubmit = true;
            }
        }

        // 관리자는 항상 제출 가능
        if (currentUser.getIsAdmin()) {
            canSubmit = true;
        }

        if (!canSubmit) {
            throw new RuntimeException("이 과제에 제출할 권한이 없습니다.");
        }

        Submission submission = Submission.builder()
                .contents(requestDTO.getContents())
                .taskId(requestDTO.getTaskId())
                .createdById(currentUser.getId())
                .build();

        Submission savedSubmission = submissionRepository.save(submission);

        // 관계가 설정된 완전한 제출 정보를 다시 조회
        Submission completeSubmission = submissionRepository.findById(savedSubmission.getId())
                .orElseThrow(() -> new RuntimeException("생성 후 제출물을 찾을 수 없습니다."));

        return SubmissionResponseDTO.fromEntity(completeSubmission);
    }

    /**
     * 모든 제출 목록을 조회합니다.
     * @return SubmissionResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> getAllSubmissions() {
        List<Submission> submissions = submissionRepository.findAll();
        return submissions.stream()
                .map(SubmissionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 제출을 조회합니다.
     * @param id 조회할 제출 ID
     * @return SubmissionResponseDTO
     */
    @Transactional(readOnly = true)
    public SubmissionResponseDTO getSubmissionById(Integer id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다. ID: " + id));
        return SubmissionResponseDTO.fromEntity(submission);
    }

    /**
     * 특정 사용자의 모든 제출을 조회합니다.
     * @param userId 사용자 ID
     * @return SubmissionResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> getSubmissionsByUserId(Integer userId) {
        List<Submission> submissions = submissionRepository.findByCreatedById(userId);
        return submissions.stream()
                .map(SubmissionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 과제에 대한 모든 제출을 조회합니다.
     * @param taskId 과제 ID
     * @return SubmissionResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> getSubmissionsByTaskId(Integer taskId) {
        List<Submission> submissions = submissionRepository.findByTaskId(taskId);
        return submissions.stream()
                .map(SubmissionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 제출을 수정합니다.
     * @param id 수정할 제출 ID
     * @param requestDTO 제출 수정 요청 정보
     * @param currentUser 현재 로그인한 사용자 정보
     * @return 수정된 제출 정보
     */
    public SubmissionResponseDTO updateSubmission(Integer id, SubmissionRequestDTO requestDTO, User currentUser) {
        Submission existingSubmission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!existingSubmission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("작성자 또는 관리자만 이 제출물을 수정할 수 있습니다.");
        }

        // 과제가 변경되는 경우 존재하는지 확인
        if (!existingSubmission.getTaskId().equals(requestDTO.getTaskId())) {
            taskRepository.findById(requestDTO.getTaskId())
                    .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + requestDTO.getTaskId()));
        }

        // 제출 필드 업데이트
        existingSubmission.setContents(requestDTO.getContents());
        existingSubmission.setTaskId(requestDTO.getTaskId());

        Submission updatedSubmission = submissionRepository.save(existingSubmission);

        Submission completeSubmission = submissionRepository.findById(updatedSubmission.getId())
                .orElseThrow(() -> new RuntimeException("수정 후 제출물을 찾을 수 없습니다."));

        return SubmissionResponseDTO.fromEntity(completeSubmission);
    }

    /**
     * 제출을 삭제합니다.
     * @param id 삭제할 제출 ID
     * @param currentUser 현재 로그인한 사용자 정보
     */
    public void deleteSubmission(Integer id, User currentUser) {
        Submission existingSubmission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다. ID: " + id));

        // 사용자가 작성자이거나 관리자인지 확인
        if (!existingSubmission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("작성자 또는 관리자만 이 제출물을 삭제할 수 있습니다.");
        }

        submissionRepository.deleteById(id);
    }

    /**
     * 제출을 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 SubmissionResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<SubmissionResponseDTO> searchSubmissions(String searchBy, String search,
                                                                  Integer page, Integer pageSize, String sortBy, String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        Page<Submission> submissionPage;

        switch (searchBy.toLowerCase()) {
            case "contents":
                submissionPage = submissionRepository.findByContentsContaining(search, pageable);
                break;
            case "creator":
                submissionPage = submissionRepository.findByCreatedByNicknameContaining(search, pageable);
                break;
            case "task":
                submissionPage = submissionRepository.findByTaskNameContaining(search, pageable);
                break;
            case "createdby":
                try {
                    Integer createdById = Integer.parseInt(search);
                    submissionPage = submissionRepository.findByCreatedById(createdById, pageable);
                } catch (NumberFormatException e) {
                    submissionPage = submissionRepository.findByCreatedByNicknameContaining(search, pageable);
                }
                break;
            case "taskid":
                try {
                    Integer taskId = Integer.parseInt(search);
                    submissionPage = submissionRepository.findByTaskId(taskId, pageable);
                } catch (NumberFormatException e) {
                    submissionPage = submissionRepository.findByTaskNameContaining(search, pageable);
                }
                break;
            default:
                submissionPage = submissionRepository.findByAllFieldsContaining(search, pageable);
                break;
        }

        List<SubmissionResponseDTO> submissions = submissionPage.getContent().stream()
                .map(SubmissionResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.<SubmissionResponseDTO>builder()
                .data(submissions)
                .page(submissionPage.getNumber())
                .size(submissionPage.getSize())
                .totalElements(submissionPage.getTotalElements())
                .totalPages(submissionPage.getTotalPages())
                .build();
    }
}
