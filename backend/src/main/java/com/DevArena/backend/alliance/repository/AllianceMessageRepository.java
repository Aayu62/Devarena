package com.DevArena.backend.alliance.repository;

import com.DevArena.backend.alliance.entity.AllianceMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AllianceMessageRepository extends JpaRepository<AllianceMessage, Long> {

    Page<AllianceMessage> findByAllianceIdAndIsDeletedFalseOrderByCreatedAtDesc(Long allianceId, Pageable pageable);

    @Query("SELECT m FROM AllianceMessage m WHERE m.allianceId = ?1 AND m.isDeleted = false AND m.createdAt >= ?2 ORDER BY m.createdAt ASC")
    List<AllianceMessage> findRecentMessages(Long allianceId, LocalDateTime since);

    List<AllianceMessage> findByAllianceIdAndIsDeletedFalseOrderByCreatedAtDesc(Long allianceId);
}
