package com.DevArena.backend.problem.dto;

import com.DevArena.backend.problem.enums.SupportedLanguage;

/**
 * Response for GET /problems/{slug}/starter?languageId=71
 * The battleground calls this when a player selects a language
 * and pre-fills the editor with the starter code.
 */
public record StarterCodeResponse(
        Long problemId,
        SupportedLanguage language,
        int judge0Id,
        String starterCode
) {}