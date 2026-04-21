package com.DevArena.backend.user.dto;

public record UserSearchDTO(
        Long id,
        String username,
        String email,
        String rank,
        Integer rating,
        String college
) {}
