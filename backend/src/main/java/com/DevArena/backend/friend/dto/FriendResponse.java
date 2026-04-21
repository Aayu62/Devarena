package com.DevArena.backend.friend.dto;

public record FriendResponse(
        Long id,
        Long friendId,
        Long userId,
        String username,
        String rank,
        Integer rating,
        String college,
        String status
) {}
