package com.DevArena.backend.user.service;

import com.DevArena.backend.user.entity.User;
import com.DevArena.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import com.DevArena.backend.battle.repository.RatingHistoryRepository;
import com.DevArena.backend.battle.entity.RatingHistory;
import com.DevArena.backend.user.dto.LeaderboardDTO;
import com.DevArena.backend.user.dto.MyRankDTO;
import com.DevArena.backend.user.dto.UserProfileDTO;
import com.DevArena.backend.user.dto.UpdateUserProfileRequest;
import com.DevArena.backend.user.dto.UserSearchDTO;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final HttpServletRequest request;
    private final RatingHistoryRepository historyRepository;

    public User currentUser() {
        String email = (String) request.getAttribute("email");
        return repo.findByEmail(email).orElseThrow();
    }
    
    public User getUserById(Long userId) {
        return repo.findById(userId).orElseThrow();
    }

    public Page<LeaderboardDTO> getLeaderboard(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
    
        Page<User> usersPage = repo.findAllByOrderByRatingDesc(pageable);
    
        long basePosition = (long) page * size;
    
        List<LeaderboardDTO> dtoList = new java.util.ArrayList<>();
    
        long currentPosition = basePosition;
    
        for (User user : usersPage.getContent()) {
            currentPosition++;
    
            dtoList.add(new LeaderboardDTO(
                    currentPosition,
                    user.getId(),
                    user.getUsername(),
                    user.getRating(),
                    user.getRank()
            ));
        }
    
        return new org.springframework.data.domain.PageImpl<>(
                dtoList,
                pageable,
                usersPage.getTotalElements()
        );
    }


    public UserProfileDTO getCurrentUserProfile() {
    String email = (String) request.getAttribute("email");
    User user = repo.findByEmail(email).orElseThrow();

        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCollege(),
                user.getGithub(),
                user.getLinkedin(),
                user.getXp(),
                user.getRank(),
                user.getRating()
        );
    }

    public UserProfileDTO getUserProfileById(Long userId) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCollege(),
                user.getGithub(),
                user.getLinkedin(),
                user.getXp(),
                user.getRank(),
                user.getRating()
        );
    }   
    
    public List<RatingHistory> getMyRatingHistory() {
        User me = currentUser();
        return historyRepository
                .findByUserIdOrderByTimestampAsc(me.getId());
    }

    public MyRankDTO getMyRank() {

        User me = currentUser();

        long betterPlayers = repo.countByRatingGreaterThan(me.getRating());

        long myRank = betterPlayers + 1;

        long totalUsers = repo.count();

        double percentile = totalUsers == 0 ? 0 :
                (100.0 * (totalUsers - myRank)) / totalUsers;

        return new MyRankDTO(
                myRank,
                totalUsers,
                percentile,
                me.getRating()
        );
    }

    public UserProfileDTO updateUserProfile(UpdateUserProfileRequest request) {
        User user = currentUser();

        if (request.college() != null) {
            user.setCollege(request.college());
        }
        if (request.github() != null) {
            user.setGithub(request.github());
        }
        if (request.linkedin() != null) {
            user.setLinkedin(request.linkedin());
        }

        User updatedUser = repo.save(user);

        return new UserProfileDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getCollege(),
                updatedUser.getGithub(),
                updatedUser.getLinkedin(),
                updatedUser.getXp(),
                updatedUser.getRank(),
                updatedUser.getRating()
        );
    }

    public List<UserSearchDTO> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        String searchTerm = "%" + query.toLowerCase() + "%";
        List<User> users = repo.findByUsernameContainingIgnoreCase(searchTerm);

        return users.stream()
                .map(user -> new UserSearchDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRank(),
                        user.getRating(),
                        user.getCollege()
                ))
                .toList();
    }

}