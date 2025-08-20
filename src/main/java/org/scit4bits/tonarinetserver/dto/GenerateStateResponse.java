package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateStateResponse {
    private String state;
    private String nonce;
}
