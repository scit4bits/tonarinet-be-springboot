package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.FileAttachment;
import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
                .build();
    }
}
