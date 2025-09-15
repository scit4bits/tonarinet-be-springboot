package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Board;

/**
 * 게시판 정보를 전달하기 위한 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {
    private Integer id;
    private String title;
    private String description;
    private String countryCode;
    private Integer orgId;

    /**
     * Board 엔티티를 BoardDTO로 변환합니다.
     * @param board 변환할 Board 엔티티
     * @return 변환된 BoardDTO 객체
     */
    public static BoardDTO fromEntity(Board board) {
        if (board == null) {
            return null;
        }
        return BoardDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .description(board.getDescription())
                .countryCode(board.getCountryCode())
                .orgId(board.getOrgId())
                .build();
    }
}
