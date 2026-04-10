package com.ofdun.jobfinder.features.skill.exception;

public class SkillNotFoundException extends RuntimeException {
    public SkillNotFoundException(Long id) {
        super("Skill with ID " + id + " not found");
    }
}
