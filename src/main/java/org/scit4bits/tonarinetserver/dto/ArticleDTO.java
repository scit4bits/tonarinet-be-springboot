package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Article;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDTO {
    private Integer id;
    private String category;
    private String title;
    private String contents;
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer views;
    private Integer boardId;
    private Integer likedByUsers;
    private Integer replyCount;
    private List<FileAttachmentResponseDTO> files;
    private List<String> tags;

    public static ArticleDTO fromEntity(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .category(article.getCategory())
                .title(article.getTitle())
                .contents(article.getContents())
                .createdById(article.getCreatedById())
                .createdByName(article.getCreatedBy() != null ? article.getCreatedBy().getNickname() : null)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .boardId(article.getBoardId())
                .views(article.getViews())
                .files(article.getAttachments() != null
                        ? article.getAttachments().stream().map(FileAttachmentResponseDTO::fromEntity).toList()
                        : null)
                .likedByUsers(article.getLikedByUsers() != null ? article.getLikedByUsers().size() : 0)
                .replyCount(article.getReplies() != null ? article.getReplies().size() : 0)
                .tags(article.getTags() != null ? article.getTags().stream().map(t -> t.getId().getTagName()).toList()
                        : null)
                .build();
    }
}
