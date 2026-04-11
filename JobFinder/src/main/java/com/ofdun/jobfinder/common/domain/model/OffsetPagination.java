package com.ofdun.jobfinder.common.domain.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffsetPagination {
    @Min(1)
    @Max(100)
    @Builder.Default
    private int limit = 20;

    @Min(0)
    @Builder.Default
    private int offset = 0;

    private String sortBy;

    @Builder.Default private boolean sortDesc = false;
}
