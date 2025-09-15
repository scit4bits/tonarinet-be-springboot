package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Reply;

import java.time.LocalDateTime;

/**
 * 댓글 응답을 위한 DTO
 */
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

    /**
     * Reply 엔티티를 ReplyResponseDTO로 변환합니다.
     * @param reply 변환할 Reply 엔티티
     * @return 변환된 ReplyResponseDTO 객체
     */
    public static ReplyResponseDTO fromEntity(Reply reply) {
        return ReplyResponseDTO.builder()
                .id(reply.getId())
                .contents(reply.getContents())
                .createdById(reply.getCreatedById())
                .createdByName(reply.getCreatedBy() != null ? reply.getCreatedBy().getNickname() : null)
                .createdAt(reply.getCreatedAt())
                .articleId(reply.getArticleId())
                .articleTitle(reply.getArticle() != null ? reply.getArticle().getTitle() : null)
                .build();
    }
}
