package org.scit4bits.tonarinetserver.service;

import java.util.List;
import java.util.stream.Collectors;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;

    public SubmissionResponseDTO createSubmission(SubmissionRequestDTO requestDTO, User currentUser) {
        // Check if task exists
        Task task = taskRepository.findById(requestDTO.getTaskId())
            .orElseThrow(() -> new RuntimeException("Task not found with ID: " + requestDTO.getTaskId()));

        // Check if user can submit to this task (could be assigned user or team member)
        boolean canSubmit = false;
        
        // If user is directly assigned to the task
        if (task.getUserId() != null && task.getUserId().equals(currentUser.getId())) {
            canSubmit = true;
        }
        
        // If user is part of assigned team
        if (task.getTeamId() != null && task.getTeam() != null) {
            boolean isTeamMember = task.getTeam().getUsers().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
            if (isTeamMember) {
                canSubmit = true;
            }
        }
        
        // Admin can always submit
        if (currentUser.getIsAdmin()) {
            canSubmit = true;
        }

        if (!canSubmit) {
            throw new RuntimeException("You are not authorized to submit to this task");
        }

        Submission submission = Submission.builder()
            .contents(requestDTO.getContents())
            .taskId(requestDTO.getTaskId())
            .createdById(currentUser.getId())
            .build();

        Submission savedSubmission = submissionRepository.save(submission);
        
        // Fetch the complete submission with relationships
        Submission completeSubmission = submissionRepository.findById(savedSubmission.getId())
            .orElseThrow(() -> new RuntimeException("Submission not found after creation"));
        
        return SubmissionResponseDTO.fromEntity(completeSubmission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> getAllSubmissions() {
        List<Submission> submissions = submissionRepository.findAll();
        return submissions.stream()
            .map(SubmissionResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubmissionResponseDTO getSubmissionById(Integer id) {
        Submission submission = submissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));
        return SubmissionResponseDTO.fromEntity(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> getSubmissionsByUserId(Integer userId) {
        List<Submission> submissions = submissionRepository.findByCreatedById(userId);
        return submissions.stream()
            .map(SubmissionResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponseDTO> getSubmissionsByTaskId(Integer taskId) {
        List<Submission> submissions = submissionRepository.findByTaskId(taskId);
        return submissions.stream()
            .map(SubmissionResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public SubmissionResponseDTO updateSubmission(Integer id, SubmissionRequestDTO requestDTO, User currentUser) {
        Submission existingSubmission = submissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));

        // Check if user is the creator or admin
        if (!existingSubmission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("Only the submission creator or admin can update this submission");
        }

        // Verify task exists if being changed
        if (!existingSubmission.getTaskId().equals(requestDTO.getTaskId())) {
            taskRepository.findById(requestDTO.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + requestDTO.getTaskId()));
        }

        // Update submission fields
        existingSubmission.setContents(requestDTO.getContents());
        existingSubmission.setTaskId(requestDTO.getTaskId());

        Submission updatedSubmission = submissionRepository.save(existingSubmission);
        
        // Fetch the complete submission with relationships
        Submission completeSubmission = submissionRepository.findById(updatedSubmission.getId())
            .orElseThrow(() -> new RuntimeException("Submission not found after update"));
        
        return SubmissionResponseDTO.fromEntity(completeSubmission);
    }

    public void deleteSubmission(Integer id, User currentUser) {
        Submission existingSubmission = submissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));

        // Check if user is the creator or admin
        if (!existingSubmission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("Only the submission creator or admin can delete this submission");
        }

        submissionRepository.deleteById(id);
    }

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
                submissionPage = submissionRepository.findByCreatedByNameContaining(search, pageable);
                break;
            case "task":
                submissionPage = submissionRepository.findByTaskNameContaining(search, pageable);
                break;
            case "createdby":
                try {
                    Integer createdById = Integer.parseInt(search);
                    submissionPage = submissionRepository.findByCreatedById(createdById, pageable);
                } catch (NumberFormatException e) {
                    submissionPage = submissionRepository.findByCreatedByNameContaining(search, pageable);
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
