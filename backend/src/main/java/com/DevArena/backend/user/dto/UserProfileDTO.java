package com.DevArena.backend.user.dto;

public record UserProfileDTO(
        Long id,
        String username,
        String email,
        String college,
        String github,
        String linkedin,
        Integer xp,
        String rank,
        Integer rating
) {}
