package com.DevArena.backend.battle.dto;

/**
 * Request body for POST /battles/friendly/join
 */
public record JoinByCodeRequest(String joinCode) {}