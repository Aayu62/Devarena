package com.DevArena.backend.alliance.service;

import com.DevArena.backend.alliance.dto.AllianceMessageDTO;
import com.DevArena.backend.alliance.entity.AllianceMessage;
import com.DevArena.backend.alliance.repository.AllianceMessageRepository;
import com.DevArena.backend.alliance.repository.AllianceMemberRepository;
import com.DevArena.backend.user.entity.User;
import com.DevArena.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllianceChatService {

    private final AllianceMessageRepository messageRepo;
    private final AllianceMemberRepository memberRepo;
    private final UserService userService;

    // ─── SEND MESSAGE ────────────────────────────────────────────
    public AllianceMessageDTO sendMessage(Long allianceId, String content) {
        User me = userService.currentUser();

        // Verify user is a member
        memberRepo.findByAllianceIdAndUserId(allianceId, me.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this alliance"));

        AllianceMessage message = new AllianceMessage();
        message.setAllianceId(allianceId);
        message.setUserId(me.getId());
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        message.setIsDeleted(false);

        AllianceMessage saved = messageRepo.save(message);
        return buildMessageDTO(saved);
    }

    // ─── GET MESSAGES (PAGINATED) ────────────────────────────────
    public Page<AllianceMessageDTO> getMessages(Long allianceId, int page, int size) {
        User me = userService.currentUser();

        // Verify user is a member
        memberRepo.findByAllianceIdAndUserId(allianceId, me.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this alliance"));

        Pageable pageable = PageRequest.of(page, size);
        Page<AllianceMessage> messages = messageRepo.findByAllianceIdAndIsDeletedFalseOrderByCreatedAtDesc(allianceId, pageable);

        return messages.map(this::buildMessageDTO);
    }

    // ─── GET RECENT MESSAGES ────────────────────────────────────
    public List<AllianceMessageDTO> getRecentMessages(Long allianceId, int limit) {
        User me = userService.currentUser();

        // Verify user is a member
        memberRepo.findByAllianceIdAndUserId(allianceId, me.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this alliance"));

        List<AllianceMessage> messages = messageRepo.findByAllianceIdAndIsDeletedFalseOrderByCreatedAtDesc(allianceId);
        return messages.stream()
                .limit(limit)
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .map(this::buildMessageDTO)
                .toList();
    }

    // ─── DELETE MESSAGE ──────────────────────────────────────────
    public void deleteMessage(Long messageId) {
        User me = userService.currentUser();

        AllianceMessage message = messageRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Only author or leader can delete
        if (!message.getUserId().equals(me.getId())) {
            // Check if user is leader of the alliance
            memberRepo.findByAllianceIdAndUserId(message.getAllianceId(), me.getId())
                    .ifPresent(member -> {
                        if (!member.getRole().name().equals("LEADER")) {
                            throw new RuntimeException("Only author or leader can delete messages");
                        }
                    });
        }

        message.setIsDeleted(true);
        message.setUpdatedAt(LocalDateTime.now());
        messageRepo.save(message);
    }

    // ─── HELPER: Build MessageDTO ────────────────────────────────
    public Integer getActiveMemberCount(Long allianceId) {
        return memberRepo.countActiveMembers(allianceId);
    }

    // ─── HELPER: Build MessageDTO ────────────────────────────────
    private AllianceMessageDTO buildMessageDTO(AllianceMessage message) {
        var userProfile = userService.getUserProfileById(message.getUserId());

        AllianceMessageDTO dto = new AllianceMessageDTO();
        dto.setId(message.getId());
        dto.setAllianceId(message.getAllianceId());
        dto.setUserId(message.getUserId());
        dto.setUsername(userProfile.username());
        dto.setUserRank(userProfile.rank());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        dto.setIsEdited(!message.getCreatedAt().equals(message.getUpdatedAt()));

        return dto;
    }
}
