package com.DevArena.backend.battle.repository;

import com.DevArena.backend.battle.entity.MatchmakingEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchmakingRepository extends JpaRepository<MatchmakingEntry, Long> {

    Optional<MatchmakingEntry> findByUserId(Long userId);

    List<MatchmakingEntry> findAllByOrderByRatingAsc();
    boolean existsByUserId(Long userId);

}
