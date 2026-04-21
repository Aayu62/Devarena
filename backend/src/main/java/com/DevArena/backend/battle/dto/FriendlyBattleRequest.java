package com.DevArena.backend.battle.dto;

import com.DevArena.backend.common.enums.Difficulty;

/**
 * Request body for POST /battles/friendly/create
 *
 * Examples:
 *   { "difficulty": "EASY" }               → random EASY problem
 *   { "difficulty": "RANDOM" }             → random problem of any difficulty
 *   { "difficulty": "MEDIUM", "problemId": 5 } → specific problem (difficulty ignored for selection)
 */
public record FriendlyBattleRequest(
        Difficulty difficulty,   // EASY | MEDIUM | HARD | RANDOM (null treated as RANDOM)
        Long problemId           // optional — if set, picks this exact problem
) {}