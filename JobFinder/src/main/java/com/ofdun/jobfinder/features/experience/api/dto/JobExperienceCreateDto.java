package com.ofdun.jobfinder.features.experience.api.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobExperienceCreateDto {
    private String position;
    private String companyName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
