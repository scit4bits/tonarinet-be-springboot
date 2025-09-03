package org.scit4bits.tonarinetserver.dto;

import org.scit4bits.tonarinetserver.entity.FileAttachment.FileType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileAttachmentRequestDTO {
    private Boolean isPrivate;
    private FileType type;
    private Integer articleId;
}
