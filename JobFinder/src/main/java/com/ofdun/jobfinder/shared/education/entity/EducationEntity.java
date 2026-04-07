package com.ofdun.jobfinder.shared.education.entity;

import com.ofdun.jobfinder.shared.resume.enums.EducationDegree;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Year;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(schema = "jobfinder", name = "educations")
@AllArgsConstructor
@NoArgsConstructor
public class EducationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private Long resumeId;

    @NotNull
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "jobfinder.education_degree")
    private EducationDegree degree;

    @NotBlank private String institution;

    @NotBlank private String faculty;

    @NotBlank private String department; // кафедра

    @NotNull private Year graduationYear;
}
