package com.DevArena.backend.user.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import com.DevArena.backend.battle.entity.RatingHistory;
import com.DevArena.backend.user.dto.LeaderboardDTO;
import com.DevArena.backend.user.dto.MyRankDTO;
import com.DevArena.backend.user.dto.UserProfileDTO;
import com.DevArena.backend.user.dto.UpdateUserProfileRequest;
import com.DevArena.backend.user.dto.UserSearchDTO;
import com.DevArena.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public UserProfileDTO me() {
        return service.getCurrentUserProfile();
    }

    @GetMapping("/{userId}")
    public UserProfileDTO getUserProfile(@PathVariable Long userId) {
        return service.getUserProfileById(userId);
    }

    @GetMapping("/leaderboard")
    public Page<LeaderboardDTO> leaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
    
        return service.getLeaderboard(page, size);
    }
    
    @GetMapping("/me/rating-history")
    public List<RatingHistory> ratingHistory() {
        return service.getMyRatingHistory();
    }

    @GetMapping("/me/rank")
    public MyRankDTO myRank() {
        return service.getMyRank();
    }

    @PutMapping("/me")
    public UserProfileDTO updateProfile(@RequestBody UpdateUserProfileRequest request) {
        return service.updateUserProfile(request);
    }

    @GetMapping("/search")
    public List<UserSearchDTO> search(@RequestParam String query) {
        return service.searchUsers(query);
    }

}