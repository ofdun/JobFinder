package com.ofdun.jobfinder.features.vacancy.api.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DraftVacancyResponse {
    private Long id;
    private Long vacancyId;
    private Date versionTimestamp;
}

