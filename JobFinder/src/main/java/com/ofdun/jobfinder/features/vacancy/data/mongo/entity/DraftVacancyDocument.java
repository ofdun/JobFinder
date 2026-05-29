package com.ofdun.jobfinder.features.vacancy.data.mongo.entity;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "draft_vacancies")
@AllArgsConstructor
@NoArgsConstructor
public class DraftVacancyDocument {
    @Id
    private Long id;

    @NotNull private Long vacancyId;

    @NotNull private Date versionTimestamp;

    @NotNull private String snapshot;
}
