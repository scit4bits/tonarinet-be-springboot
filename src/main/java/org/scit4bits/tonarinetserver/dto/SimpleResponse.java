package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 간단한 응답 메시지를 위한 DTO
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SimpleResponse {
    private final String message;
    private String data = null;
}