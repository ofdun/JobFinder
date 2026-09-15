package com.ofdun.jobfinder.support;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.education.domain.model.EducationModel;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.experience.domain.model.JobExperienceModel;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.enums.LanguageProficiencyLevel;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.enums.EducationDegree;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public final class TestDataMother {
    private TestDataMother() {}

    public static ApplicantModel applicant() {
        return new ApplicantBuilder().build();
    }

    public static EmployerModel employer() {
        return new EmployerModel(
                1L,
                "Example Ltd",
                "hash",
                "Software",
                "Москва",
                "https://example.test",
                "employer@example.test",
                10L);
    }

    public static ApplicationModel application() {
        return new ApplicationModel(1L, 2L, 3L, new Date(1700000000000L), ApplicationStatus.NEW);
    }

    public static VacancyModel vacancy() {
        return new VacancyModel(
                1L,
                2L,
                10L,
                new BigDecimal("100000"),
                List.of(1L, 2L),
                List.of(1L),
                PaymentFrequency.values()[0],
                "3 years",
                JobFormat.values()[0],
                EmploymentType.values()[0],
                "Java developer",
                new Date(1700000000000L),
                "Москва",
                VacancyStatus.ACTIVE);
    }

    public static DraftVacancyModel draft() {
        return new DraftVacancyModel(
                1L, 2L, new Date(1700000000000L), "{\"description\":\"Java developer\"}");
    }

    public static ResumeModel resume() {
        return new ResumeModel(
                1L,
                2L,
                3L,
                "Java developer",
                List.of(1L),
                new ArrayList<>(),
                new ArrayList<>(),
                List.of(1L),
                new Date(1700000000000L),
                List.of(0.1f, 0.2f));
    }

    public static EducationModel education() {
        return new EducationModel(1L, 2L, EducationDegree.values()[0], "BMSTU", "IU", "IU7", 2025);
    }

    public static JobExperienceModel experience() {
        return new JobExperienceModel(
                1L,
                2L,
                "Developer",
                "Example Ltd",
                "Java",
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2024, 1, 1));
    }

    public static CategoryModel category() {
        return new CategoryModel(1L, "IT");
    }

    public static SkillModel skill() {
        return new SkillModel(1L, "Java");
    }

    public static LanguageModel language() {
        return new LanguageModel(1L, "English", LanguageProficiencyLevel.values()[0]);
    }

    public static LocationModel location() {
        return new LocationModel(1L, "Москва", "Россия");
    }
}
