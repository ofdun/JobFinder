package com.ofdun.jobfinder.features.applicant.api.mapper;

import com.ofdun.jobfinder.features.applicant.api.dto.ApplicantRequest;
import com.ofdun.jobfinder.features.applicant.api.dto.ApplicantResponse;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.location.api.mapper.LocationApiMapper;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicantApiMapper {
    private final EncryptionService encryptionService;

    public ApplicantModel toModel(ApplicantRequest request) {
        if (request == null) return null;
        return new ApplicantModel(
                null,
                request.getName(),
                request.getEmail(),
                encryptionService.encrypt(request.getPassword()),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getLocationId()
        );
    }

    public ApplicantResponse toResponse(ApplicantModel model, LocationModel location) {
        if (model == null) return null;
        return new ApplicantResponse(
                model.getId(),
                model.getName(),
                model.getEmail(),
                model.getAddress(),
                model.getPhoneNumber(),
                LocationApiMapper.toDto(location)
        );
    }
}

