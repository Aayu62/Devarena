package com.DevArena.backend.battle.repository;

import com.DevArena.backend.battle.entity.Battle;
import com.DevArena.backend.battle.entity.BattleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {

    boolean existsByPlayer1AndStatus(Long player1, BattleStatus status);
    boolean existsByPlayer2AndStatus(Long player2, BattleStatus status);

    List<Battle> findByPlayer1OrPlayer2OrderByStartedAtDesc(Long p1, Long p2);

    /** Used by the friend join-by-code flow. */
    Optional<Battle> findByJoinCode(String joinCode);

    /** Pessimistic write lock — used inside finishBattleInternal to prevent double-finish. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Battle b WHERE b.id = :id")
    Optional<Battle> findByIdForUpdate(@Param("id") Long id);
}