package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 파일 첨부 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final ArticleRepository articleRepository;
    private final SubmissionRepository submissionRepository;

    @Value("${upload.path:c:/upload}")
    private String uploadPath;

    /**
     * 여러 파일을 업로드합니다.
     * @param files 업로드할 파일 리스트
     * @param requestDTO 파일 메타데이터 요청 정보
     * @param currentUser 현재 로그인한 사용자 정보
     * @return 업로드된 파일 정보 DTO 리스트
     */
    public List<FileAttachmentResponseDTO> uploadFiles(List<MultipartFile> files, FileAttachmentRequestDTO requestDTO, User currentUser) {
        try {
            List<FileAttachmentResponseDTO> dtos = new ArrayList<>();
            Article article = null;
            Submission submission = null;

            if (requestDTO.getArticleId() != null) {
                article = articleRepository.findById(requestDTO.getArticleId()).get();
            }

            if (requestDTO.getSubmissionId() != null) {
                submission = submissionRepository.findById(requestDTO.getSubmissionId()).get();
            }
            for (MultipartFile file : files) {
                // 파일 유효성 검사
                if (file.isEmpty()) {
                    throw new RuntimeException("파일이 비어있습니다.");
                }

                // 게시글에 파일을 첨부할 수 있는지 확인 (게시글 작성자 또는 관리자)
                if (article != null && !article.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                    throw new AccessDeniedException("이 게시글에 파일을 첨부할 권한이 없습니다.");
                }

                // 제출물에 파일을 첨부할 수 있는지 확인 (제출물 작성자 또는 관리자)
                if (submission != null && !submission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                    throw new AccessDeniedException("이 제출물에 파일을 첨부할 권한이 없습니다.");
                }

                // 고유한 파일 이름 생성
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID() + fileExtension;

                // 파일 유형 결정 (지정되지 않은 경우)
                FileType fileType = requestDTO.getType();
                if (fileType == null) {
                    fileType = determineFileType(file.getContentType(), fileExtension);
                }

                // 파일을 디스크에 저장
                Path filePath = Paths.get(uploadPath, uniqueFilename);
                Files.copy(file.getInputStream(), filePath);

                // FileAttachment 엔티티 생성
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

                // 관계가 설정된 완전한 엔티티를 다시 조회
                FileAttachment completeFile = fileAttachmentRepository.findById(savedFile.getId())
                        .orElseThrow(() -> new RuntimeException("업로드 후 파일을 찾을 수 없습니다."));

                dtos.add(FileAttachmentResponseDTO.fromEntity(completeFile));
            }

            return dtos;

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 모든 파일 첨부 목록을 조회합니다.
     * @return FileAttachmentResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getAllFileAttachments() {
        List<FileAttachment> files = fileAttachmentRepository.findAll();
        return files.stream()
                .map(FileAttachmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 파일 첨부를 조회합니다.
     * @param id 조회할 파일 ID
     * @param currentUser 현재 로그인한 사용자 정보
     * @return FileAttachmentResponseDTO
     */
    @Transactional(readOnly = true)
    public FileAttachmentResponseDTO getFileAttachmentById(Integer id, User currentUser) {
        FileAttachment fileAttachment = fileAttachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. ID: " + id));

        // 비공개 파일 접근 권한 확인
        if (fileAttachment.getIsPrivate() && !currentUser.getIsAdmin()) {
            throw new RuntimeException("이 비공개 파일에 접근할 권한이 없습니다.");
        }

        return FileAttachmentResponseDTO.fromEntity(fileAttachment);
    }

    /**
     * 특정 게시글에 첨부된 모든 파일을 조회합니다.
     * @param articleId 게시글 ID
     * @param currentUser 현재 로그인한 사용자 정보
     * @return FileAttachmentResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsByArticleId(Integer articleId, User currentUser) {
        List<FileAttachment> files = fileAttachmentRepository.findByArticleId(articleId);

        // 사용자가 접근할 수 없는 비공개 파일 필터링
        return files.stream()
                .filter(file -> !file.getIsPrivate() ||
                        file.getUploadedBy().equals(currentUser.getId()) ||
                        currentUser.getIsAdmin())
                .map(FileAttachmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 제출물에 첨부된 모든 파일을 조회합니다.
     * @param submissionId 제출물 ID
     * @param currentUser 현재 로그인한 사용자 정보
     * @return FileAttachmentResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsBySubmissionId(Integer submissionId, User currentUser) {
        List<FileAttachment> files = fileAttachmentRepository.findBySubmissionId(submissionId);

        // 사용자가 접근할 수 없는 비공개 파일 필터링
        return files.stream()
                .filter(file -> !file.getIsPrivate() ||
                        file.getUploadedBy().equals(currentUser.getId()) ||
                        currentUser.getIsAdmin())
                .map(FileAttachmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 업로드한 모든 파일을 조회합니다.
     * @param userId 사용자 ID
     * @param currentUser 현재 로그인한 사용자 정보
     * @return FileAttachmentResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsByUserId(Integer userId, User currentUser) {
        // 자신의 파일 또는 관리자만 조회 가능
        if (!userId.equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("다른 사용자가 업로드한 파일을 볼 권한이 없습니다.");
        }

        List<FileAttachment> files = fileAttachmentRepository.findByUploadedBy(userId);
        return files.stream()
                .map(FileAttachmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 유형의 모든 파일을 조회합니다.
     * @param type 파일 유형
     * @return FileAttachmentResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FileAttachmentResponseDTO> getFileAttachmentsByType(FileType type) {
        List<FileAttachment> files = fileAttachmentRepository.findByType(type);
        return files.stream()
                .map(FileAttachmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 파일 첨부 정보를 수정합니다.
     * @param id 수정할 파일 ID
     * @param requestDTO 파일 수정 요청 정보
     * @param currentUser 현재 로그인한 사용자 정보
     * @return 수정된 파일 정보 DTO
     */
    public FileAttachmentResponseDTO updateFileAttachment(Integer id, FileAttachmentRequestDTO requestDTO, User currentUser) {
        FileAttachment existingFile = fileAttachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. ID: " + id));

        // 업로더 또는 관리자인지 확인
        if (!existingFile.getUploadedBy().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("파일 업로더 또는 관리자만 이 파일을 수정할 수 있습니다.");
        }

        // 게시글 변경 시 유효성 검사
        if (requestDTO.getArticleId() != null && !requestDTO.getArticleId().equals(existingFile.getArticleId())) {
            Article article = articleRepository.findById(requestDTO.getArticleId())
                    .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + requestDTO.getArticleId()));

            // 새로운 게시글에 첨부할 권한이 있는지 확인
            if (!article.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                throw new RuntimeException("이 게시글에 파일을 첨부할 권한이 없습니다.");
            }
        }

        // 제출물 변경 시 유효성 검사
        if (requestDTO.getSubmissionId() != null && !requestDTO.getSubmissionId().equals(existingFile.getSubmissionId())) {
            Submission submission = submissionRepository.findById(requestDTO.getSubmissionId())
                    .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다. ID: " + requestDTO.getSubmissionId()));

            // 새로운 제출물에 첨부할 권한이 있는지 확인
            if (!submission.getCreatedById().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
                throw new RuntimeException("이 제출물에 파일을 첨부할 권한이 없습니다.");
            }
        }

        // 필드 업데이트
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

        // 관계가 설정된 완전한 엔티티를 다시 조회
        FileAttachment completeFile = fileAttachmentRepository.findById(updatedFile.getId())
                .orElseThrow(() -> new RuntimeException("수정 후 파일을 찾을 수 없습니다."));

        return FileAttachmentResponseDTO.fromEntity(completeFile);
    }

    /**
     * 파일 첨부를 삭제합니다.
     * @param id 삭제할 파일 ID
     * @param currentUser 현재 로그인한 사용자 정보
     */
    public void deleteFileAttachment(Integer id, User currentUser) {
        FileAttachment existingFile = fileAttachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. ID: " + id));

        // 업로더 또는 관리자인지 확인
        if (!existingFile.getUploadedBy().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("파일 업로더 또는 관리자만 이 파일을 삭제할 수 있습니다.");
        }

        try {
            // 물리적 파일 삭제
            Path filePath = Paths.get(existingFile.getFilepath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 오류를 기록하지만 데이터베이스 삭제는 계속 진행
            System.err.println("물리적 파일 삭제 실패: " + e.getMessage());
        }

        // 데이터베이스에서 삭제
        fileAttachmentRepository.deleteById(id);
    }

    /**
     * 파일 첨부를 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @param currentUser 현재 로그인한 사용자 정보
     * @return 페이징 처리된 FileAttachmentResponseDTO
     */
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

        // 현재 사용자가 접근할 수 없는 비공개 파일 필터링
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
                .totalElements(files.size()) // 필터링된 결과에 맞게 조정
                .totalPages(filePage.getTotalPages())
                .build();
    }

    /**
     * 파일 유형을 결정합니다.
     * @param contentType MIME 타입
     * @param fileExtension 파일 확장자
     * @return 파일 유형 (IMAGE 또는 ATTACHMENT)
     */
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

    /**
     * 파일을 다운로드합니다.
     * @param id 다운로드할 파일 ID
     * @param currentUser 현재 로그인한 사용자 정보
     * @return 파일의 바이트 배열
     */
    @Transactional(readOnly = true)
    public byte[] downloadFile(Integer id, User currentUser) {
        FileAttachment fileAttachment = fileAttachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다. ID: " + id));

        // 비공개 파일 접근 권한 확인
        if (fileAttachment.getIsPrivate() &&
                !fileAttachment.getUploadedBy().equals(currentUser.getId()) &&
                !currentUser.getIsAdmin()) {
            throw new RuntimeException("이 비공개 파일을 다운로드할 권한이 없습니다.");
        }

        try {
            Path filePath = Paths.get(fileAttachment.getFilepath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + e.getMessage(), e);
        }
    }
}
