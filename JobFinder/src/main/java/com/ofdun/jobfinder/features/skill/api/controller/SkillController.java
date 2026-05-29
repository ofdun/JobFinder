package com.ofdun.jobfinder.features.skill.api.controller;

import com.ofdun.jobfinder.features.skill.api.dto.SkillDto;
import com.ofdun.jobfinder.features.skill.api.mapper.SkillApiMapper;
import com.ofdun.jobfinder.features.skill.domain.service.SkillService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<List<SkillDto>> getSkills() {
        var skills = skillService.getAllSkills().stream().map(SkillApiMapper::toDto).toList();
        return ResponseEntity.ok(skills);
    }
}

