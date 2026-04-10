package com.ofdun.jobfinder.features.matching.api.controller;

import com.ofdun.jobfinder.features.matching.api.dto.MatchResultResponse;
import com.ofdun.jobfinder.features.matching.api.mapper.MatchingApiMapper;
import com.ofdun.jobfinder.features.matching.domain.service.MatchingService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;
    private final MatchingApiMapper mapper;

    @GetMapping("/vacancies/{vacancyId}/candidates")
    @PreAuthorize("@sec.employerOwnsVacancy(authentication, #vacancyId)")
    public ResponseEntity<@NonNull List<MatchResultResponse>> findSuitableCandidates(
            @PathVariable Long vacancyId,
            @RequestParam(required = false, defaultValue = "10") Integer maxAmount) {
        var matchResults = matchingService.findSuitableCandidates(vacancyId, maxAmount);
        return ResponseEntity.ok(mapper.toResponseList(matchResults));
    }
}
