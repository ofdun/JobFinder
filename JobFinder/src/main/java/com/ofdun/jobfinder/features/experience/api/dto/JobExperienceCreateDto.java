package com.ofdun.jobfinder.features.experience.api.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobExperienceCreateDto {
    @jakarta.validation.constraints.NotBlank private String position;
    @jakarta.validation.constraints.NotBlank private String companyName;
    private String description;
    @jakarta.validation.constraints.NotNull private LocalDate startDate;
    private LocalDate endDate;

    @jakarta.validation.constraints.AssertTrue
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
}
