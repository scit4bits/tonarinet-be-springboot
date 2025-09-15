package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 댓글 생성 및 수정을 위한 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyRequestDTO {

    @NotBlank(message = "Contents is required")
    private String contents;

    @NotNull(message = "Article ID is required")
    private Integer articleId;
}
