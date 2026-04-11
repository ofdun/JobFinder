package com.ofdun.jobfinder.common.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
