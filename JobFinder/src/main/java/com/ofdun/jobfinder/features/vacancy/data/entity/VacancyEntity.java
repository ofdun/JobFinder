package com.ofdun.jobfinder.features.vacancy.data.entity;

import com.ofdun.jobfinder.shared.location.entity.LocationEntity;
import com.ofdun.jobfinder.shared.skill.entity.SkillEntity;
import com.ofdun.jobfinder.shared.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.shared.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.shared.vacancy.enums.PaymentFrequency;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(schema = "jobfinder", name = "vacancies")
@AllArgsConstructor
@NoArgsConstructor
public class VacancyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private Long employerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    @NotNull
    @Valid
    private LocationEntity location;

    @NotNull private BigDecimal salary;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "jobfinder",
            name = "vacancy_skills",
            joinColumns = @JoinColumn(name = "vacancy_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<@NotNull SkillEntity> skills;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentFrequency paymentFrequency;

    @NotBlank private String workExperience;

    @NotNull
    @Enumerated(EnumType.STRING)
    private JobFormat workFormat;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @NotBlank private String description;

    @NotNull private Date publicationDate;

    @NotBlank private String address;
}
