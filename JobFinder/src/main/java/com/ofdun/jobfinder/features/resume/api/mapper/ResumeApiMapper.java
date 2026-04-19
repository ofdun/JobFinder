package com.ofdun.jobfinder.features.resume.api.mapper;

import com.ofdun.jobfinder.features.category.api.mapper.CategoryApiMapper;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.education.api.mapper.EducationApiMapper;
import com.ofdun.jobfinder.features.experience.api.mapper.JobExperienceApiMapper;
import com.ofdun.jobfinder.features.language.api.mapper.LanguageApiMapper;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeRequest;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeResponse;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeUpdateRequest;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.skill.api.mapper.SkillApiMapper;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ResumeApiMapper {
    public ResumeModel toModel(ResumeRequest request) {
        if (request == null) {
            return null;
        }
        return new ResumeModel(
                null,
                request.getApplicantId(),
                request.getCategoryId(),
                request.getDescription(),
                request.getSkillIds(),
                request.getEducations() == null
                        ? null
                        : request.getEducations().stream()
                                .map(EducationApiMapper::toEducationModel)
                                .toList(),
                request.getJobExperiences() == null
                        ? null
                        : request.getJobExperiences().stream()
                                .map(JobExperienceApiMapper::toJobExperienceModel)
                                .toList(),
                request.getLanguageIds(),
                null);
    }

    public ResumeModel toModel(ResumeUpdateRequest request) {
        if (request == null) {
            return null;
        }
        return new ResumeModel(
                null,
                request.getApplicantId(),
                request.getCategoryId(),
                request.getDescription(),
                request.getSkillIds(),
                request.getEducations() == null
                        ? null
                        : request.getEducations().stream()
                                .map(EducationApiMapper::toEducationModel)
                                .toList(),
                request.getJobExperiences() == null
                        ? null
                        : request.getJobExperiences().stream()
                                .map(JobExperienceApiMapper::toJobExperienceModel)
                                .toList(),
                request.getLanguageIds(),
                null);
    }

    public ResumeResponse toResponse(
            ResumeModel resume,
            CategoryModel category,
            List<SkillModel> skills,
            List<LanguageModel> languages) {
        if (resume == null) {
            return null;
        }
        return new ResumeResponse(
                resume.getId(),
                resume.getApplicantId(),
                CategoryApiMapper.toDto(category),
                resume.getDescription(),
                skills == null ? null : skills.stream().map(SkillApiMapper::toDto).toList(),
                resume.getEducations() == null
                        ? null
                        : resume.getEducations().stream()
                                .map(EducationApiMapper::toEducationDto)
                                .toList(),
                resume.getJobExperiences() == null
                        ? null
                        : resume.getJobExperiences().stream()
                                .map(JobExperienceApiMapper::toJobExperienceDto)
                                .toList(),
                languages == null
                        ? null
                        : languages.stream().map(LanguageApiMapper::toDto).toList(),
                resume.getDate());
    }
}
