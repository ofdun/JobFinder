package com.ofdun.jobfinder.features.application.data.mongo.entity;

import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "applications")
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDocument {
    @Id
    private Long id;

    @NotNull private Long vacancyId;

    @NotNull private Long resumeId;

    @NotNull private Date applicationDate;

    @NotNull private ApplicationStatus applicationStatus;
}

