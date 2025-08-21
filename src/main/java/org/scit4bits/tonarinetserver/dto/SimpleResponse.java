package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SimpleResponse {
    private final String message;
    private String data = null;
}