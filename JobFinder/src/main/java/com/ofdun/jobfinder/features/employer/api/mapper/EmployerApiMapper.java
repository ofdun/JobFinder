package com.ofdun.jobfinder.features.employer.api.mapper;

import com.ofdun.jobfinder.features.employer.api.dto.EmployerRequest;
import com.ofdun.jobfinder.features.employer.api.dto.EmployerResponse;
import com.ofdun.jobfinder.features.employer.api.dto.EmployerUpdateRequest;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import com.ofdun.jobfinder.features.location.api.mapper.LocationApiMapper;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployerApiMapper {
    private final EncryptionService encryptionService;

    public EmployerModel toModel(EmployerRequest request) {
        if (request == null) return null;

        return new EmployerModel(
                null,
                request.getName(),
                encryptionService.encrypt(request.getPassword()),
                request.getDescription(),
                request.getAddress(),
                request.getWebsiteLink(),
                request.getEmail(),
                request.getLocationId());
    }

    public EmployerModel toModel(EmployerUpdateRequest request, String currentPasswordHash) {
        if (request == null) return null;

        String passwordHash = currentPasswordHash;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordHash = encryptionService.encrypt(request.getPassword());
        }

        return new EmployerModel(
                null,
                request.getName(),
                passwordHash,
                request.getDescription(),
                request.getAddress(),
                request.getWebsiteLink(),
                request.getEmail(),
                request.getLocationId());
    }

    public EmployerResponse toResponse(EmployerModel model, LocationModel location) {
        if (model == null) return null;

        return new EmployerResponse(
                model.getId(),
                model.getName(),
                model.getEmail(),
                model.getDescription(),
                model.getAddress(),
                model.getWebsiteLink(),
                LocationApiMapper.toDto(location));
    }
}
