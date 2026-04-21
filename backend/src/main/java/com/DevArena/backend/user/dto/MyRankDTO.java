package com.DevArena.backend.user.dto;

public record MyRankDTO(
        long rank,
        long totalUsers,
        double percentile,
        int rating
) {}