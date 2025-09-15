package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 작성을 위한 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardWriteRequestDTO {
    private String title;
    private String content;
    private List<String> tags;
    private String category;
}
    