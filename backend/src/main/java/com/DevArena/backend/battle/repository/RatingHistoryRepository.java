package com.DevArena.backend.battle.repository;

import com.DevArena.backend.battle.entity.RatingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingHistoryRepository 
        extends JpaRepository<RatingHistory, Long> {

    List<RatingHistory> findByUserIdOrderByTimestampAsc(Long userId);
    Optional<RatingHistory> findFirstByUserIdAndBattleIdOrderByTimestampDesc(Long userId, Long battleId);
}
