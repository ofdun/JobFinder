package com.ofdun.jobfinder.features.resume.domain.chain;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

public abstract class ResumeHandler {
    private ResumeHandler next;

    public ResumeHandler setNext(ResumeHandler next) {
        this.next = next;
        return next;
    }

    public ResumeModel handle(ResumeModel resume) {
        ResumeModel processed = execute(resume);
        if (next != null) {
            return next.handle(processed);
        }
        return processed;
    }

    protected abstract ResumeModel execute(ResumeModel resume);
}
