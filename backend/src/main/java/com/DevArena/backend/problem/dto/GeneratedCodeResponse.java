package com.DevArena.backend.problem.dto;

import com.DevArena.backend.problem.enums.SupportedLanguage;

import java.util.List;

/**
 * Response for GET /admin/problems/{id}/generated-code
 * Shows the auto-generated starter code and driver for each language.
 * Admin can review and override each one if needed.
 */
public record GeneratedCodeResponse(
        Long problemId,
        String functionName,
        List<LanguageCode> languages
) {
    public record LanguageCode(
            SupportedLanguage language,
            int judge0Id,
            String displayName,
            String starterCode,
            String driverCode
    ) {}
}