package com.ofdun.jobfinder.features.applicant.api.mapper;

import com.ofdun.jobfinder.features.applicant.api.dto.ApplicantRequest;
import com.ofdun.jobfinder.features.applicant.api.dto.ApplicantResponse;
import com.ofdun.jobfinder.features.applicant.api.dto.ApplicantUpdateRequest;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import com.ofdun.jobfinder.features.location.api.mapper.LocationApiMapper;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicantApiMapper {
    private final EncryptionService encryptionService;

    @Observed(
            name = "jobfinder.applicant.mapper.toModel",
            contextualName = "Подготовка модели соискателя",
            lowCardinalityKeyValues = {"feature", "applicant", "layer", "mapper"})
    public ApplicantModel toModel(ApplicantRequest request) {
        if (request == null) return null;
        return new ApplicantModel(
                null,
                request.getName(),
                request.getEmail(),
                encryptionService.encrypt(request.getPassword()),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getLocationId());
    }

    @Observed(
            name = "jobfinder.applicant.mapper.toModel",
            contextualName = "Подготовка модели соискателя",
            lowCardinalityKeyValues = {"feature", "applicant", "layer", "mapper"})
    public ApplicantModel toModel(ApplicantUpdateRequest request, String currentPasswordHash) {
        if (request == null) return null;

        String passwordHash = currentPasswordHash;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordHash = encryptionService.encrypt(request.getPassword());
        }

        return new ApplicantModel(
                null,
                request.getName(),
                request.getEmail(),
                passwordHash,
                request.getAddress(),
                request.getPhoneNumber(),
                request.getLocationId());
    }

    @Observed(
            name = "jobfinder.applicant.mapper.toResponse",
            contextualName = "Подготовка ответа API для соискателя",
            lowCardinalityKeyValues = {"feature", "applicant", "layer", "mapper"})
    public ApplicantResponse toResponse(ApplicantModel model, LocationModel location) {
        if (model == null) return null;
        return new ApplicantResponse(
                model.getId(),
                model.getName(),
                model.getEmail(),
                model.getAddress(),
                model.getPhoneNumber(),
                LocationApiMapper.toDto(location));
    }
}
