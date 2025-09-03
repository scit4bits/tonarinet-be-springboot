package org.scit4bits.tonarinetserver.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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
    