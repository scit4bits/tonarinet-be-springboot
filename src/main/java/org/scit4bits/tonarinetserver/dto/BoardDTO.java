package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Board;

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
