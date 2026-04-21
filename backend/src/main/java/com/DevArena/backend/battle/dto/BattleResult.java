package com.DevArena.backend.battle.dto;

import com.DevArena.backend.battle.entity.BattleStatus;
import com.DevArena.backend.battle.entity.BattleType;

/**
 * Full battle result — scores are only non-null when status == FINISHED.
 * During ACTIVE/WAITING the score fields are null to prevent information leakage.
 */
public record BattleResult(
        BattleStatus status,
        BattleType type,
        Long winnerId,
        Long player1Id,
        Long player2Id,
        String player1Username,
        String player2Username,
        Integer player1Score,
        Integer player1Total,
        Integer player2Score,
        Integer player2Total,
        Integer player1RatingChange,
        Integer player2RatingChange,
        String problemTitle,
        Long battleDuration
) {}