package com.DevArena.backend.battle.repository;

import com.DevArena.backend.battle.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Submission findTopByBattleIdAndUserIdOrderBySubmittedAtDesc(Long battleId, Long userId);

}
