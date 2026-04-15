package com.ofdun.jobfinder.cli.dto.common;

public record Language(Long id, String name, ProficiencyLevel proficiencyLevel) {
    public enum ProficiencyLevel {
        A1,
        A2,
        B1,
        B2,
        C1,
        C2
    }
}
