package com.ofdun.jobfinder.features.location.api.controller;

import com.ofdun.jobfinder.features.location.api.dto.LocationDto;
import com.ofdun.jobfinder.features.location.api.mapper.LocationApiMapper;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(LocationApiMapper.toDto(locationService.getLocationById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationDto>> searchLocations(
            @RequestParam String q, @RequestParam(defaultValue = "20") int limit) {
        var items =
                locationService.searchLocations(q, limit).stream()
                        .map(LocationApiMapper::toDto)
                        .toList();
        return ResponseEntity.ok(items);
    }
}

