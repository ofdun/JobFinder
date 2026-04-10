package com.ofdun.jobfinder.features.resume.domain.chain;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

import java.util.Optional;

public abstract class ResumeHandler {
    private ResumeHandler next;

    public ResumeHandler setNext(ResumeHandler next) {
        this.next = next;
        return next;
    }

    public Optional<ResumeModel> handle(ResumeModel resume) {
        var processed = execute(resume);
        if (next != null && processed.isPresent()) {
            return next.handle(processed.get());
        }
        return processed;
    }

    protected abstract Optional<ResumeModel> execute(ResumeModel resume);
}
