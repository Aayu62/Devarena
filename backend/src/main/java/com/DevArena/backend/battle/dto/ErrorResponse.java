package com.DevArena.backend.battle.dto;

public record ErrorResponse(
        boolean success,
        String message,
        int status
) {}
