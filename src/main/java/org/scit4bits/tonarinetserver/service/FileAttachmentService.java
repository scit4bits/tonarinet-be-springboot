package org.scit4bits.tonarinetserver.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.scit4bits.tonarinetserver.dto.FileAttachmentRequestDTO;
import org.scit4bits.tonarinetserver.dto.FileAttachmentResponseDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.Article;
import org.scit4bits.tonarinetserver.entity.FileAttachment;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;
import org.scit4bits.tonarinetserver.entity.Submission;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.ArticleRepository;
import org.scit4bits.tonarinetserver.repository.FileAttachmentRepository;
import org.scit4bits.tonarinetserver.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final ArticleRepository articleRepository;
    private final SubmissionRepository submissionRepository;

    @Value("${upload.path:c:/upload}")
    private String uploadPath;

    public List<FileAttachmentResponseDTO> uploadFiles(List<MultipartFile> files, FileAttachmentRequestDTO requestDTO, User currentUser) {
        try {
            List<FileAttachmentResponseDTO> dtos = new ArrayList<>();
            Article article = null;
            Submission submission = null;
            
            if(requestDTO.getArticleId() != null){
                article = articleRepository.findById(requestDTO.getArticleId()).get();
            }
            
            if(requestDTO.getSubmissionId() != null){
                submission = submissionRepository.findById(requestDTO.getSubmissionId()).get();
            }
            for(MultipartFile file: files){
    // Validate file
                if (file.isEmpty()) {
                    throw new RuntimeException("File is empty");
                }

                // Check if user can attach to this article (article creator or admin)
                if (article != null && !article.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                    throw new AccessDeniedException("You are not authorized to attach files to this article");
                }

                // Check if user can attach to this submission (submission creator or admin)
                if (submission != null && !submission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                    throw new AccessDeniedException("You are not authorized to attach files to this submission");
                }

                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Determine file type if not specified
                FileType fileType = requestDTO.getType();
                if (fileType == null) {
                    fileType = determineFileType(file.getContentType(), fileExtension);
                }

                // Save file to disk
                Path filePath = Paths.get(uploadPath, uniqueFilename);
                Files.copy(file.getInputStream(), filePath);

                // Create FileAttachment entity
                FileAttachment fileAttachment = FileAttachment.builder()
                    .filepath(filePath.toString())
                    .originalFilename(originalFilename)
                    .isPrivate(requestDTO.getIsPrivate() != null ? requestDTO.getIsPrivate() : false)
                    .uploadedBy(currentUser.getId())
                    .type(fileType)
                    .articleId(requestDTO.getArticleId() != null ? requestDTO.getArticleId() : null)
                    .submissionId(requestDTO.getSubmissionId() != null ? requestDTO.getSubmissionId() : null)
                    .filesize((int) file.getSize())
                    .build();

                FileAttachment savedFile = fileAttachmentRepository.save(fileAttachment);
                
                // Fetch complete entity with relationships
                FileAttachment completeFile = fileAttachmentRepository.findById(savedFile.getId())
                    .orElseThrow(() -> new RuntimeException("File not found after upload"));

                dtos.add(FileAttachmentResponseDTO.fromEntity(completeFile));
            }

            return dtos;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getAllFileAttachments() {
        List<FileAttachment> files = fileAttachmentRepository.findAll();
        return files.stream()
            .map(FileAttachmentResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileAttachmentResponseDTO getFileAttachmentById(Integer id, User currentUser) {
        FileAttachment fileAttachment = fileAttachmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));
        
        // Check privacy permissions
        if (fileAttachment.getIsPrivate() && !currentUser.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to access this private file");
        }
        
        return FileAttachmentResponseDTO.fromEntity(fileAttachment);
    }

    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsByArticleId(Integer articleId, User currentUser) {
        List<FileAttachment> files = fileAttachmentRepository.findByArticleId(articleId);
        
        // Filter out private files that user cannot access
        return files.stream()
            .filter(file -> !file.getIsPrivate() || 
                          file.getUploadedBy().equals(currentUser.getId()) || 
                          currentUser.getIsAdmin())
            .map(FileAttachmentResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsBySubmissionId(Integer submissionId, User currentUser) {
        List<FileAttachment> files = fileAttachmentRepository.findBySubmissionId(submissionId);
        
        // Filter out private files that user cannot access
        return files.stream()
            .filter(file -> !file.getIsPrivate() || 
                          file.getUploadedBy().equals(currentUser.getId()) || 
                          currentUser.getIsAdmin())
            .map(FileAttachmentResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsByUserId(Integer userId, User currentUser) {
        // Only allow users to see their own files or admin to see all
        if (!userId.equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to view files uploaded by other users");
        }
        
        List<FileAttachment> files = fileAttachmentRepository.findByUploadedBy(userId);
        return files.stream()
            .map(FileAttachmentResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsByType(FileType type) {
        List<FileAttachment> files = fileAttachmentRepository.findByType(type);
        return files.stream()
            .map(FileAttachmentResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public FileAttachmentResponseDTO updateFileAttachment(Integer id, FileAttachmentRequestDTO requestDTO, User currentUser) {
        FileAttachment existingFile = fileAttachmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));

        // Check if user is the uploader or admin
        if (!existingFile.getUploadedBy().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("Only the file uploader or admin can update this file");
        }

        // Validate article if being changed
        if (requestDTO.getArticleId() != null && !requestDTO.getArticleId().equals(existingFile.getArticleId())) {
            Article article = articleRepository.findById(requestDTO.getArticleId())
                .orElseThrow(() -> new RuntimeException("Article not found with ID: " + requestDTO.getArticleId()));
            
            // Check if user can attach to the new article
            if (!article.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                throw new RuntimeException("You are not authorized to attach files to this article");
            }
        }

        // Validate submission if being changed
        if (requestDTO.getSubmissionId() != null && !requestDTO.getSubmissionId().equals(existingFile.getSubmissionId())) {
            Submission submission = submissionRepository.findById(requestDTO.getSubmissionId())
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + requestDTO.getSubmissionId()));
            
            // Check if user can attach to the new submission
            if (!submission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                throw new RuntimeException("You are not authorized to attach files to this submission");
            }
        }

        // Update fields
        if (requestDTO.getIsPrivate() != null) {
            existingFile.setIsPrivate(requestDTO.getIsPrivate());
        }
        if (requestDTO.getType() != null) {
            existingFile.setType(requestDTO.getType());
        }
        if (requestDTO.getArticleId() != null) {
            existingFile.setArticleId(requestDTO.getArticleId());
        }
        if (requestDTO.getSubmissionId() != null) {
            existingFile.setSubmissionId(requestDTO.getSubmissionId());
        }

        FileAttachment updatedFile = fileAttachmentRepository.save(existingFile);
        
        // Fetch complete entity with relationships
        FileAttachment completeFile = fileAttachmentRepository.findById(updatedFile.getId())
            .orElseThrow(() -> new RuntimeException("File not found after update"));
        
        return FileAttachmentResponseDTO.fromEntity(completeFile);
    }

    public void deleteFileAttachment(Integer id, User currentUser) {
        FileAttachment existingFile = fileAttachmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));

        // Check if user is the uploader or admin
        if (!existingFile.getUploadedBy().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("Only the file uploader or admin can delete this file");
        }

        try {
            // Delete physical file
            Path filePath = Paths.get(existingFile.getFilepath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }

        // Delete from database
        fileAttachmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FileAttachmentResponseDTO> searchFileAttachments(String searchBy, String search, 
            Integer page, Integer pageSize, String sortBy, String sortDirection, User currentUser) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
        
        Page<FileAttachment> filePage;
        
        switch (searchBy.toLowerCase()) {
            case "filename":
                filePage = fileAttachmentRepository.findByOriginalFilenameContainingIgnoreCase(search, pageable);
                break;
            case "uploader":
                filePage = fileAttachmentRepository.findByUploaderNameContaining(search, pageable);
                break;
            case "article":
                filePage = fileAttachmentRepository.findByArticleTitleContaining(search, pageable);
                break;
            case "submission":
                filePage = fileAttachmentRepository.findBySubmissionContentsContaining(search, pageable);
                break;
            case "type":
                try {
                    FileType fileType = FileType.valueOf(search.toUpperCase());
                    filePage = fileAttachmentRepository.findByType(fileType, pageable);
                } catch (IllegalArgumentException e) {
                    filePage = fileAttachmentRepository.findByAllFieldsContaining(search, pageable);
                }
                break;
            case "uploadedby":
                try {
                    Integer uploadedBy = Integer.parseInt(search);
                    filePage = fileAttachmentRepository.findByUploadedBy(uploadedBy, pageable);
                } catch (NumberFormatException e) {
                    filePage = fileAttachmentRepository.findByUploaderNameContaining(search, pageable);
                }
                break;
            case "articleid":
                try {
                    Integer articleId = Integer.parseInt(search);
                    filePage = fileAttachmentRepository.findByArticleId(articleId, pageable);
                } catch (NumberFormatException e) {
                    filePage = fileAttachmentRepository.findByArticleTitleContaining(search, pageable);
                }
                break;
            case "submissionid":
                try {
                    Integer submissionId = Integer.parseInt(search);
                    filePage = fileAttachmentRepository.findBySubmissionId(submissionId, pageable);
                } catch (NumberFormatException e) {
                    filePage = fileAttachmentRepository.findBySubmissionContentsContaining(search, pageable);
                }
                break;
            default:
                filePage = fileAttachmentRepository.findByAllFieldsContaining(search, pageable);
                break;
        }
        
        // Filter out private files that current user cannot access
        List<FileAttachmentResponseDTO> files = filePage.getContent().stream()
            .filter(file -> !file.getIsPrivate() || 
                          file.getUploadedBy().equals(currentUser.getId()) || 
                          currentUser.getIsAdmin())
            .map(FileAttachmentResponseDTO::fromEntity)
            .collect(Collectors.toList());
        
        return PagedResponse.<FileAttachmentResponseDTO>builder()
            .data(files)
            .page(filePage.getNumber())
            .size(filePage.getSize())
            .totalElements((long) files.size()) // Adjusted for filtered results
            .totalPages(filePage.getTotalPages())
            .build();
    }

    private FileType determineFileType(String contentType, String fileExtension) {
        if (contentType != null && contentType.startsWith("image/")) {
            return FileType.IMAGE;
        }
        
        String extension = fileExtension.toLowerCase();
        if (extension.matches("\\.(jpg|jpeg|png|gif|bmp|svg|webp)")) {
            return FileType.IMAGE;
        }
        
        return FileType.ATTACHMENT;
    }

    @Transactional(readOnly = true)
    public byte[] downloadFile(Integer id, User currentUser) {
        FileAttachment fileAttachment = fileAttachmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));
        
        // Check privacy permissions
        if (fileAttachment.getIsPrivate() && 
            !fileAttachment.getUploadedBy().equals(currentUser.getId()) && 
            !currentUser.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to download this private file");
        }
        
        try {
            Path filePath = Paths.get(fileAttachment.getFilepath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }
}
