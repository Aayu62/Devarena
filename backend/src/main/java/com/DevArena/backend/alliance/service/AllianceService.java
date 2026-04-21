package com.DevArena.backend.alliance.service;

import com.DevArena.backend.alliance.dto.*;
import com.DevArena.backend.alliance.entity.*;
import com.DevArena.backend.alliance.repository.*;
import com.DevArena.backend.friend.repository.FriendRepository;
import com.DevArena.backend.user.entity.User;
import com.DevArena.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllianceService {

    private final AllianceRepository allianceRepo;
    private final AllianceMemberRepository memberRepo;
    private final AllianceNameChangeHistoryRepository nameChangeRepo;
    private final UserService userService;
    private final FriendRepository friendRepo;

    // ─── CREATE ALLIANCE ─────────────────────────────────────────
    public AllianceDetailDTO create(String name) {
        User me = userService.currentUser();

        Alliance a = new Alliance();
        a.setName(name);
        a.setLeaderId(me.getId());
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());

        Alliance saved = allianceRepo.save(a);

        // Add creator as LEADER
        AllianceMember m = new AllianceMember();
        m.setAllianceId(saved.getId());
        m.setUserId(me.getId());
        m.setRole(AllianceRole.LEADER);
        m.setJoinedAt(LocalDateTime.now());
        m.setIsActive(true);

        memberRepo.save(m);

        return buildAllianceDetailDTO(saved, me.getId());
    }

    // ─── JOIN ALLIANCE ───────────────────────────────────────────
    public void join(Long allianceId) {
        User me = userService.currentUser();
        Alliance alliance = allianceRepo.findById(allianceId)
                .orElseThrow(() -> new RuntimeException("Alliance not found"));

        // Check if already a member
        memberRepo.findByAllianceIdAndUserId(allianceId, me.getId())
                .ifPresentOrElse(
                    member -> {
                        if (!member.getIsActive()) {
                            member.setIsActive(true);
                            memberRepo.save(member);
                        }
                    },
                    () -> {
                        AllianceMember m = new AllianceMember();
                        m.setAllianceId(allianceId);
                        m.setUserId(me.getId());
                        m.setRole(AllianceRole.MEMBER);
                        m.setJoinedAt(LocalDateTime.now());
                        m.setIsActive(true);
                        memberRepo.save(m);
                    }
                );
    }

    // ─── LEAVE ALLIANCE ──────────────────────────────────────────
    public void leaveAlliance(Long allianceId) {
        User me = userService.currentUser();
        Alliance alliance = allianceRepo.findById(allianceId)
                .orElseThrow(() -> new RuntimeException("Alliance not found"));

        // Cannot leave if you're the only leader
        if (alliance.getLeaderId().equals(me.getId())) {
            long leaderCount = memberRepo.findByAllianceIdAndRoleOrderByJoinedAtAsc(allianceId, AllianceRole.LEADER).size();
            if (leaderCount <= 1) {
                throw new RuntimeException("Cannot leave alliance. You are the only leader. Transfer leadership first.");
            }
        }

        AllianceMember member = memberRepo.findByAllianceIdAndUserId(allianceId, me.getId())
                .orElseThrow(() -> new RuntimeException("Not a member of this alliance"));
        member.setIsActive(false);
        memberRepo.save(member);
    }

    // ─── REMOVE MEMBER (Leader only) ──────────────────────────────
    public void removeMember(Long allianceId, Long memberId) {
        User me = userService.currentUser();
        Alliance alliance = allianceRepo.findById(allianceId)
                .orElseThrow(() -> new RuntimeException("Alliance not found"));

        if (!alliance.getLeaderId().equals(me.getId())) {
            throw new RuntimeException("Only alliance leader can remove members");
        }

        if (alliance.getLeaderId().equals(memberId)) {
            throw new RuntimeException("Cannot remove leader. Transfer leadership first.");
        }

        AllianceMember member = memberRepo.findByAllianceIdAndUserId(allianceId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found in this alliance"));

        member.setIsActive(false);
        memberRepo.save(member);
    }

    // ─── ADD FRIEND TO ALLIANCE (Leader only) ────────────────────
    public AllianceMemberDTO addFriendToAlliance(Long allianceId, Long friendId) {
        User me = userService.currentUser();
        Alliance alliance = allianceRepo.findById(allianceId)
                .orElseThrow(() -> new RuntimeException("Alliance not found"));

        if (!alliance.getLeaderId().equals(me.getId())) {
            throw new RuntimeException("Only alliance leader can add members");
        }

        // Check if they are friends
        boolean areFriends = friendRepo.findByUserIdAndFriendIdAndStatus(me.getId(), friendId, "ACCEPTED").isPresent() ||
                             friendRepo.findByUserIdAndFriendIdAndStatus(friendId, me.getId(), "ACCEPTED").isPresent();

        if (!areFriends) {
            throw new RuntimeException("Can only add friends to alliance");
        }

        // Check member count
        Integer activeMembers = memberRepo.countActiveMembers(allianceId);
        if (activeMembers >= alliance.getMaxMembers()) {
            throw new RuntimeException("Alliance is full");
        }

        // Add member
        AllianceMember member = memberRepo.findByAllianceIdAndUserId(allianceId, friendId)
                .orElse(new AllianceMember());

        member.setAllianceId(allianceId);
        member.setUserId(friendId);
        member.setRole(AllianceRole.MEMBER);
        member.setIsActive(true);
        if (member.getJoinedAt() == null) {
            member.setJoinedAt(LocalDateTime.now());
        }

        AllianceMember saved = memberRepo.save(member);
        return buildAllianceMemberDTO(saved);
    }

    // ─── CHANGE ALLIANCE NAME (Leader only) ──────────────────────
    public AllianceDetailDTO changeAllianceName(Long allianceId, String newName) {
        User me = userService.currentUser();
        Alliance alliance = allianceRepo.findById(allianceId)
                .orElseThrow(() -> new RuntimeException("Alliance not found"));

        if (!alliance.getLeaderId().equals(me.getId())) {
            throw new RuntimeException("Only alliance leader can change name");
        }

        // Check name change quota (max 3 times per 90 days)
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        List<AllianceNameChangeHistory> recentChanges = nameChangeRepo.findRecentChanges(allianceId, ninetyDaysAgo);

        if (recentChanges.size() >= 3) {
            LocalDateTime nextAllowedChange = recentChanges.get(recentChanges.size() - 1).getChangedAt().plusDays(90);
            throw new RuntimeException("Name change quota exceeded. Next change allowed at: " + nextAllowedChange);
        }

        // Record the change
        AllianceNameChangeHistory history = new AllianceNameChangeHistory();
        history.setAllianceId(allianceId);
        history.setOldName(alliance.getName());
        history.setNewName(newName);
        history.setChangedByUserId(me.getId());
        history.setChangedAt(LocalDateTime.now());
        nameChangeRepo.save(history);

        // Update alliance name
        alliance.setName(newName);
        alliance.setUpdatedAt(LocalDateTime.now());
        allianceRepo.save(alliance);

        return buildAllianceDetailDTO(alliance, me.getId());
    }

    // ─── TRANSFER LEADERSHIP ────────────────────────────────────
    public AllianceDetailDTO transferLeadership(Long allianceId, Long newLeaderId) {
        User me = userService.currentUser();
        Alliance alliance = allianceRepo.findById(allianceId)
                .orElseThrow(() -> new RuntimeException("Alliance not found"));

        if (!alliance.getLeaderId().equals(me.getId())) {
            throw new RuntimeException("Only current leader can transfer leadership");
        }

        // Verify new leader is a member
        AllianceMember newLeaderMember = memberRepo.findByAllianceIdAndUserId(allianceId, newLeaderId)
                .orElseThrow(() -> new RuntimeException("Target user is not a member of this alliance"));

        // Update old leader to OFFICER
        AllianceMember oldLeaderMember = memberRepo.findByAllianceIdAndUserId(allianceId, me.getId()).get();
        oldLeaderMember.setRole(AllianceRole.OFFICER);
        memberRepo.save(oldLeaderMember);

        // Update new leader to LEADER
        newLeaderMember.setRole(AllianceRole.LEADER);
        memberRepo.save(newLeaderMember);

        // Update alliance leaderId
        alliance.setLeaderId(newLeaderId);
        alliance.setUpdatedAt(LocalDateTime.now());
        Alliance saved = allianceRepo.save(alliance);

        return buildAllianceDetailDTO(saved, me.getId());
    }

    // ─── GET ALLIANCE DETAILS ───────────────────────────────────
    public AllianceDetailDTO getAllianceDetail(Long allianceId) {
        User me = userService.currentUser();
        Alliance alliance = allianceRepo.findById(allianceId)
                .orElseThrow(() -> new RuntimeException("Alliance not found"));
        return buildAllianceDetailDTO(alliance, me.getId());
    }

    // ─── GET USER'S ALLIANCES ───────────────────────────────────
    public List<AllianceDetailDTO> getUserAlliances(Long userId) {
        List<AllianceMember> memberships = memberRepo.findByUserIdAndIsActiveTrueOrderByJoinedAtDesc(userId);
        return memberships.stream()
                .map(m -> {
                    Alliance a = allianceRepo.findById(m.getAllianceId()).orElse(null);
                    return a != null ? buildAllianceDetailDTO(a, userId) : null;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    // ─── GET MY ALLIANCES ───────────────────────────────────────
    public List<AllianceDetailDTO> getMyAlliances() {
        User me = userService.currentUser();
        return getUserAlliances(me.getId());
    }

    // ─── GET ALLIANCE MEMBERS ───────────────────────────────────
    public List<AllianceMemberDTO> getAllianceMembers(Long allianceId) {
        List<AllianceMember> members = memberRepo.findByAllianceIdAndIsActiveTrueOrderByJoinedAtAsc(allianceId);
        return members.stream()
                .map(this::buildAllianceMemberDTO)
                .toList();
    }

    // ─── HELPER: Build AllianceDetailDTO ─────────────────────────
    private AllianceDetailDTO buildAllianceDetailDTO(Alliance alliance, Long currentUserId) {
        var leaderProfile = userService.getUserProfileById(alliance.getLeaderId());

        Integer memberCount = memberRepo.countActiveMembers(alliance.getId());

        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        List<AllianceNameChangeHistory> recentChanges = nameChangeRepo.findRecentChanges(alliance.getId(), ninetyDaysAgo);
        Integer nameChangeCount = recentChanges.size();
        LocalDateTime nextNameChangeAt = null;
        if (nameChangeCount >= 3) {
            nextNameChangeAt = recentChanges.get(recentChanges.size() - 1).getChangedAt().plusDays(90);
        }

        AllianceRole currentUserRole = memberRepo.findByAllianceIdAndUserId(alliance.getId(), currentUserId)
                .map(AllianceMember::getRole)
                .orElse(null);

        AllianceDetailDTO dto = new AllianceDetailDTO();
        dto.setId(alliance.getId());
        dto.setName(alliance.getName());
        dto.setDescription(alliance.getDescription());
        dto.setLeaderId(alliance.getLeaderId());
        dto.setLeaderUsername(leaderProfile.username());
        dto.setMaxMembers(alliance.getMaxMembers());
        dto.setMemberCount(memberCount);
        dto.setCreatedAt(alliance.getCreatedAt());
        dto.setUpdatedAt(alliance.getUpdatedAt());
        dto.setCurrentUserRole(currentUserRole);
        dto.setNameChangeCount(nameChangeCount);
        dto.setNextNameChangeAt(nextNameChangeAt);

        return dto;
    }

    // ─── HELPER: Build AllianceMemberDTO ─────────────────────────
    private AllianceMemberDTO buildAllianceMemberDTO(AllianceMember member) {
        var userProfile = userService.getUserProfileById(member.getUserId());

        AllianceMemberDTO dto = new AllianceMemberDTO();
        dto.setId(member.getId());
        dto.setUserId(member.getUserId());
        dto.setAllianceId(member.getAllianceId());
        dto.setUsername(userProfile.username());
        dto.setRank(userProfile.rank());
        dto.setRating(userProfile.rating());
        dto.setCollege(userProfile.college());
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
        dto.setIsOnline(false); // Will be tracked via WebSocket later

        return dto;
    }
}
