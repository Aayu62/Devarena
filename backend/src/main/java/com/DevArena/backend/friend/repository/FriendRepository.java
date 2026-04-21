package com.DevArena.backend.friend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.DevArena.backend.friend.entity.Friend;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserIdAndStatus(Long userId, String status);
    List<Friend> findByFriendIdAndStatus(Long friendId, String status);
    Friend findByUserIdAndFriendId(Long userId, Long friendId);
    Optional<Friend> findByUserIdAndFriendIdAndStatus(Long userId, Long friendId, String status);
}