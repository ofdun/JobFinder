package com.ofdun.jobfinder.features.matching.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchResultModel {
    private Long id;
    private Float score;
}
