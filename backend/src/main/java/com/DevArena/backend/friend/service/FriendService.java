package com.DevArena.backend.friend.service;

import com.DevArena.backend.friend.entity.Friend;
import com.DevArena.backend.friend.repository.FriendRepository;
import com.DevArena.backend.friend.dto.FriendResponse;
import com.DevArena.backend.user.entity.User;
import com.DevArena.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository repo;
    private final UserRepository userRepo;

    public void sendRequest(Long from, Long to) {
        Friend f = new Friend();
        f.setUserId(from);
        f.setFriendId(to);
        f.setStatus("PENDING");
        repo.save(f);
    }

    public void accept(Long from, Long to) {
        Friend f = repo.findByUserIdAndFriendId(from, to);
        f.setStatus("ACCEPTED");
        repo.save(f);

        Friend reverse = new Friend();
        reverse.setUserId(to);
        reverse.setFriendId(from);
        reverse.setStatus("ACCEPTED");
        repo.save(reverse);
    }

    public List<FriendResponse> list(Long userId) {
        List<Friend> friends = repo.findByUserIdAndStatus(userId, "ACCEPTED");
        return friends.stream()
                .map(friend -> {
                    User friendUser = userRepo.findById(friend.getFriendId()).orElse(null);
                    if (friendUser != null) {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getFriendId(),
                                friend.getUserId(),
                                friendUser.getUsername(),
                                friendUser.getRank(),
                                friendUser.getRating(),
                                friendUser.getCollege(),
                                friend.getStatus()
                        );
                    }
                    return null;
                })
                .filter(r -> r != null)
                .toList();
    }

    public List<FriendResponse> getPendingReceived(Long userId) {
        List<Friend> friends = repo.findByFriendIdAndStatus(userId, "PENDING");
        return friends.stream()
                .map(friend -> {
                    User requesterUser = userRepo.findById(friend.getUserId()).orElse(null);
                    if (requesterUser != null) {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getFriendId(),
                                friend.getUserId(),
                                requesterUser.getUsername(),
                                requesterUser.getRank(),
                                requesterUser.getRating(),
                                requesterUser.getCollege(),
                                friend.getStatus()
                        );
                    }
                    return null;
                })
                .filter(r -> r != null)
                .toList();
    }

    public List<FriendResponse> getPendingSent(Long userId) {
        List<Friend> friends = repo.findByUserIdAndStatus(userId, "PENDING");
        return friends.stream()
                .map(friend -> {
                    User recipientUser = userRepo.findById(friend.getFriendId()).orElse(null);
                    if (recipientUser != null) {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getFriendId(),
                                friend.getUserId(),
                                recipientUser.getUsername(),
                                recipientUser.getRank(),
                                recipientUser.getRating(),
                                recipientUser.getCollege(),
                                friend.getStatus()
                        );
                    }
                    return null;
                })
                .filter(r -> r != null)
                .toList();
    }
}
