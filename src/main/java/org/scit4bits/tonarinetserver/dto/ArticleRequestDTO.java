package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시글 생성 및 수정을 위한 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleRequestDTO {

    private String category;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Contents is required")
    private String contents;

    @NotNull(message = "Board ID is required")
    private Integer boardId;
}
