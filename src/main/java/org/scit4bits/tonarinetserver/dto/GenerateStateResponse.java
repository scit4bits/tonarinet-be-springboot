package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 상태(state) 및 nonce 값을 포함하는 응답 DTO
 */
@Data
@AllArgsConstructor
public class GenerateStateResponse {
    private String state;
    private String nonce;
}
