package com.DevArena.backend.battle.dto;

public record BattleEvent(
        String type,     // STARTED, SUBMITTED, FINISHED
        Long battleId,
        Long userId,
        String message
) {}
