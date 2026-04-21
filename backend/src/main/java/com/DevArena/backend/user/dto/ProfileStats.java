package com.DevArena.backend.user.dto;

public record ProfileStats(
        Long totalBattles,
        Long wins,
        Long losses,
        Long draws,
        Integer rating,
        Double winRate
) {}
