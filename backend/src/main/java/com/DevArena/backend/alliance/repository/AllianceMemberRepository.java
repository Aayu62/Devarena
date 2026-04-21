package com.DevArena.backend.alliance.repository;

import com.DevArena.backend.alliance.entity.AllianceMember;
import com.DevArena.backend.alliance.entity.AllianceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllianceMemberRepository extends JpaRepository<AllianceMember, Long> {

    List<AllianceMember> findByAllianceId(Long allianceId);

    List<AllianceMember> findByAllianceIdAndIsActiveTrueOrderByJoinedAtAsc(Long allianceId);

    List<AllianceMember> findByUserId(Long userId);

    List<AllianceMember> findByUserIdAndIsActiveTrueOrderByJoinedAtDesc(Long userId);

    Optional<AllianceMember> findByAllianceIdAndUserId(Long allianceId, Long userId);

    @Query("SELECT COUNT(m) FROM AllianceMember m WHERE m.allianceId = ?1 AND m.isActive = true")
    Integer countActiveMembers(Long allianceId);

    List<AllianceMember> findByAllianceIdAndRoleOrderByJoinedAtAsc(Long allianceId, AllianceRole role);
}