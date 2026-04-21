package com.DevArena.backend.user.dto;

public record LeaderboardDTO(
        Long position,
        Long id,
        String username,
        Integer rating,
        String rank
) {}
