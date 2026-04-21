package com.DevArena.backend.battle.dto;

import com.DevArena.backend.battle.entity.BattleStatus;
import com.DevArena.backend.battle.entity.BattleType;
import com.DevArena.backend.common.enums.Difficulty;

/**
 * Returned after creating a friendly battle.
 * The creator shares joinCode with their friend out-of-band (chat, DM, etc.)
 */
public record FriendlyBattleResponse(
        Long battleId,
        String joinCode,       // e.g. "A3F9K2" — share this with your friend
        BattleStatus status,
        BattleType type,
        Difficulty difficulty,
        Long problemId
) {}