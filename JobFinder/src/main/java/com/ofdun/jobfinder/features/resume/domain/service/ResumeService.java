package com.ofdun.jobfinder.features.resume.domain.service;

import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import java.util.Optional;

public interface ResumeService {
    Long createResume(ResumeModel resumeModel);

    Optional<ResumeModel> getResumeById(Long resumeId);

    Optional<ResumeModel> updateResume(ResumeModel resumeModel);

    Boolean deleteResume(Long resumeId);

    PageResult<ResumeModel> searchResumes(ResumeSearchFilter filter, OffsetPagination pagination);
}
