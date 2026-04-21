package com.DevArena.backend.friend.controller;

import com.DevArena.backend.friend.dto.FriendResponse;
import com.DevArena.backend.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService service;

    @PostMapping("/request")
    public void request(@RequestParam Long from, @RequestParam Long to) {
        service.sendRequest(from, to);
    }

    @PostMapping("/accept")
    public void accept(@RequestParam Long from, @RequestParam Long to) {
        service.accept(from, to);
    }

    @GetMapping("/list")
    public List<FriendResponse> list(@RequestParam Long userId) {
        return service.list(userId);
    }

    @GetMapping("/pending/received")
    public List<FriendResponse> pendingReceived(@RequestParam Long userId) {
        return service.getPendingReceived(userId);
    }

    @GetMapping("/pending/sent")
    public List<FriendResponse> pendingSent(@RequestParam Long userId) {
        return service.getPendingSent(userId);
    }
}