package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 파티 생성 및 수정을 위한 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartyRequestDTO {

    @NotBlank(message = "Party name is required")
    private String name;
}
