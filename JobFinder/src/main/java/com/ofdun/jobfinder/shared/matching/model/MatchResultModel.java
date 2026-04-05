package com.ofdun.jobfinder.shared.matching.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchResultModel {
    private Long id;
    private Float score;
}
