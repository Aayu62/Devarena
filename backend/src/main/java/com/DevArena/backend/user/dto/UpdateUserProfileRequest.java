package com.DevArena.backend.user.dto;

public record UpdateUserProfileRequest(
        String college,
        String github,
        String linkedin
) {}
