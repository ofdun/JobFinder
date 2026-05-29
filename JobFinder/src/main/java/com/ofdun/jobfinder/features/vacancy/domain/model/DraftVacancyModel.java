package com.ofdun.jobfinder.features.vacancy.domain.model;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DraftVacancyModel {
    private Long id;

    @NotNull private Long vacancyId;

    @NotNull private Date versionTimestamp;

    @NotNull private String snapshot;
}
