package com.DevArena.backend.battle.dto;

import java.util.List;

public record RunRequest(
        Long problemId,
        String code,
        Integer languageId,
        List<CustomTestCase> customTestCases   // player-created, never persisted
) {
    /**
     * A test case the player typed themselves in the battleground.
     * These are never stored server-side — they travel only in this request.
     */
    public record CustomTestCase(
            String input,
            String expected   // optional; can be empty string
    ) {}
}