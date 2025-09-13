package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
