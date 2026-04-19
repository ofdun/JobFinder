package com.ofdun.jobfinder.features.resume.domain.repository;

import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import java.util.Optional;

public interface RelationalResumeRepository {
    Long createResume(ResumeModel resumeModel);

    Optional<ResumeModel> getResumeById(Long resumeId);

    ResumeModel updateResume(ResumeModel resumeModel);

    Boolean deleteResume(Long resumeId);

    PageResult<ResumeModel> searchResumes(
            ResumeSearchFilter filter, int limit, int offset, String sortBy, boolean sortDesc);
}
