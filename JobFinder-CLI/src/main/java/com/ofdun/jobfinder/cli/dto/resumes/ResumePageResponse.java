package com.ofdun.jobfinder.cli.dto.resumes;

import java.util.List;

public record ResumePageResponse(
        List<Resume> items, Integer page, Integer size, Long totalElements, Integer totalPages) {}
