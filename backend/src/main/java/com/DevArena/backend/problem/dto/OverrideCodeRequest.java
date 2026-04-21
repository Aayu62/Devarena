package com.DevArena.backend.problem.dto;

/**
 * PUT /admin/problems/{id}/template/{languageId}
 * PUT /admin/problems/{id}/driver/{languageId}
 *
 * Allows admin to replace an auto-generated starter code or driver
 * with a hand-written version.
 */
public record OverrideCodeRequest(String code) {}