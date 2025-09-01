package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.Reply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyResponseDTO {
    private Integer id;
    private String contents;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private Integer articleId;
    private String articleTitle;

    public static ReplyResponseDTO fromEntity(Reply reply) {
        return ReplyResponseDTO.builder()
                .id(reply.getId())
                .contents(reply.getContents())
                .createdById(reply.getCreatedById())
                .createdByName(reply.getCreatedBy() != null ? reply.getCreatedBy().getName() : null)
                .createdAt(reply.getCreatedAt())
                .articleId(reply.getArticleId())
                .articleTitle(reply.getArticle() != null ? reply.getArticle().getTitle() : null)
                .build();
    }
}
