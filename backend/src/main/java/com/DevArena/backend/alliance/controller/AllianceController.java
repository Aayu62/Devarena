package com.DevArena.backend.alliance.controller;

import com.DevArena.backend.alliance.dto.*;
import com.DevArena.backend.alliance.service.AllianceService;
import com.DevArena.backend.alliance.service.AllianceChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alliances")
@RequiredArgsConstructor
public class AllianceController {

    private final AllianceService service;
    private final AllianceChatService chatService;

    // ─── CREATE ALLIANCE ─────────────────────────────────────────
    @PostMapping("/create")
    public AllianceDetailDTO create(@RequestParam String name) {
        return service.create(name);
    }

    // ─── JOIN ALLIANCE ───────────────────────────────────────────
    @PostMapping("/join")
    public void join(@RequestParam Long allianceId) {
        service.join(allianceId);
    }

    // ─── LEAVE ALLIANCE ──────────────────────────────────────────
    @PostMapping("/{allianceId}/leave")
    public void leaveAlliance(@PathVariable Long allianceId) {
        service.leaveAlliance(allianceId);
    }

    // ─── GET ALLIANCE DETAILS ────────────────────────────────────
    @GetMapping("/{allianceId}")
    public AllianceDetailDTO getAllianceDetail(@PathVariable Long allianceId) {
        return service.getAllianceDetail(allianceId);
    }

    // ─── GET USER'S ALLIANCES ───────────────────────────────────
    @GetMapping("/my-alliances")
    public List<AllianceDetailDTO> getMyAlliances() {
        return service.getMyAlliances();
    }

    // ─── GET ALLIANCE MEMBERS ────────────────────────────────────
    @GetMapping("/{allianceId}/members")
    public List<AllianceMemberDTO> getMembers(@PathVariable Long allianceId) {
        return service.getAllianceMembers(allianceId);
    }

    // ─── REMOVE MEMBER (Leader only) ──────────────────────────────
    @DeleteMapping("/{allianceId}/members/{memberId}")
    public void removeMember(@PathVariable Long allianceId, @PathVariable Long memberId) {
        service.removeMember(allianceId, memberId);
    }

    // ─── ADD FRIEND TO ALLIANCE (Leader only) ────────────────────
    @PostMapping("/{allianceId}/members/{friendId}")
    public AllianceMemberDTO addFriendToAlliance(@PathVariable Long allianceId, @PathVariable Long friendId) {
        return service.addFriendToAlliance(allianceId, friendId);
    }

    // ─── CHANGE ALLIANCE NAME (Leader only) ──────────────────────
    @PutMapping("/{allianceId}/name")
    public AllianceDetailDTO changeAllianceName(@PathVariable Long allianceId, @RequestBody NameChangeRequest request) {
        return service.changeAllianceName(allianceId, request.getNewName());
    }

    // ─── TRANSFER LEADERSHIP ────────────────────────────────────
    @PostMapping("/{allianceId}/transfer-leadership")
    public AllianceDetailDTO transferLeadership(@PathVariable Long allianceId, @RequestBody TransferLeadershipRequest request) {
        return service.transferLeadership(allianceId, request.getNewLeaderId());
    }

    // ─── CHAT: GET MESSAGES (PAGINATED) ──────────────────────────
    @GetMapping("/{allianceId}/chat/messages")
    public Page<AllianceMessageDTO> getMessages(
            @PathVariable Long allianceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return chatService.getMessages(allianceId, page, size);
    }

    // ─── CHAT: GET RECENT MESSAGES ──────────────────────────────
    @GetMapping("/{allianceId}/chat/recent")
    public List<AllianceMessageDTO> getRecentMessages(
            @PathVariable Long allianceId,
            @RequestParam(defaultValue = "50") int limit) {
        return chatService.getRecentMessages(allianceId, limit);
    }

    // ─── CHAT: SEND MESSAGE ─────────────────────────────────────
    @PostMapping("/{allianceId}/chat/message")
    public AllianceMessageDTO sendMessage(
            @PathVariable Long allianceId,
            @RequestBody SendMessageRequest request) {
        return chatService.sendMessage(allianceId, request.getContent());
    }

    // ─── CHAT: DELETE MESSAGE ───────────────────────────────────
    @DeleteMapping("/{allianceId}/chat/messages/{messageId}")
    public void deleteMessage(@PathVariable Long allianceId, @PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
    }

    // ─── GET ACTIVE MEMBER COUNT (For real-time updates) ─────────
    @GetMapping("/{allianceId}/member-count")
    public MemberCountResponse getMemberCount(@PathVariable Long allianceId) {
        return new MemberCountResponse(chatService.getActiveMemberCount(allianceId));
    }

    // ─── HELPER CLASSES ─────────────────────────────────────────
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NameChangeRequest {
        private String newName;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransferLeadershipRequest {
        private Long newLeaderId;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SendMessageRequest {
        private String content;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MemberCountResponse {
        private Integer count;
    }
}
