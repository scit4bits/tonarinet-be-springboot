package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.FileAttachment;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;

import java.time.LocalDateTime;

/**
 * 파일 첨부 응답을 위한 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileAttachmentResponseDTO {
    private Integer id;
    private String filepath;
    private String originalFilename;
    private Boolean isPrivate;
    private Integer uploadedBy;
    private String uploadedByName;
    private FileType type;
    private LocalDateTime uploadedAt;
    private Integer articleId;
    private String articleTitle;
    private Integer filesize;
    private Integer submissionId;
    private String submissionContents;

    /**
     * FileAttachment 엔티티를 FileAttachmentResponseDTO로 변환합니다.
     * @param fileAttachment 변환할 FileAttachment 엔티티
     * @return 변환된 FileAttachmentResponseDTO 객체
     */
    public static FileAttachmentResponseDTO fromEntity(FileAttachment fileAttachment) {
        return FileAttachmentResponseDTO.builder()
                .id(fileAttachment.getId())
                .filepath(fileAttachment.getFilepath())
                .originalFilename(fileAttachment.getOriginalFilename())
                .isPrivate(fileAttachment.getIsPrivate())
                .uploadedBy(fileAttachment.getUploadedBy())
                .uploadedByName(fileAttachment.getUploadedByUser() != null ?
                        fileAttachment.getUploadedByUser().getName() : null)
                .type(fileAttachment.getType())
                .uploadedAt(fileAttachment.getUploadedAt())
                .articleId(fileAttachment.getArticleId())
                .articleTitle(fileAttachment.getArticle() != null ?
                        fileAttachment.getArticle().getTitle() : null)
                .filesize(fileAttachment.getFilesize())
                .submissionId(fileAttachment.getSubmissionId())
                .submissionContents(fileAttachment.getSubmission() != null ?
                        fileAttachment.getSubmission().getContents() : null)
                .build();
    }
}
