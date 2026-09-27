package com.ofdun.jobfinder.support;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;

public final class ApplicantBuilder {
    private Long id = 1L;
    private String name = "Иван Иванов";
    private String email = "applicant@example.test";
    private Long locationId = 10L;

    public ApplicantBuilder withId(Long value) {
        id = value;
        return this;
    }

    public ApplicantBuilder withName(String value) {
        name = value;
        return this;
    }

    public ApplicantBuilder withEmail(String value) {
        email = value;
        return this;
    }

    public ApplicantBuilder withLocationId(Long value) {
        locationId = value;
        return this;
    }

    public ApplicantModel build() {
        return new ApplicantModel(
                id, name, email, "test-password-hash", "Москва", "+7 999 123 45 67", locationId);
    }
}
