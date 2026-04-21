package com.DevArena.backend.alliance.repository;

import com.DevArena.backend.alliance.entity.AllianceNameChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AllianceNameChangeHistoryRepository extends JpaRepository<AllianceNameChangeHistory, Long> {

    @Query("SELECT h FROM AllianceNameChangeHistory h WHERE h.allianceId = ?1 AND h.changedAt >= ?2 ORDER BY h.changedAt DESC")
    List<AllianceNameChangeHistory> findRecentChanges(Long allianceId, LocalDateTime since);

    List<AllianceNameChangeHistory> findByAllianceIdOrderByChangedAtDesc(Long allianceId);
}
