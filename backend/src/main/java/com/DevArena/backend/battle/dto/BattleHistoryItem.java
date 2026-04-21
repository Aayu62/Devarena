package com.DevArena.backend.battle.dto;

import com.DevArena.backend.battle.entity.BattleStatus;
import com.DevArena.backend.battle.entity.BattleType;

public record BattleHistoryItem(
        Long battleId,
        Long player1Id,
        Long player2Id,
        Long opponentId,
        BattleType type,
        BattleStatus status,
        String result,
        Long winnerId,
        Long startedAt
) {}
