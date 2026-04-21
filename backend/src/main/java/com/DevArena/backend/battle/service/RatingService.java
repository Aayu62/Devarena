package com.DevArena.backend.battle.service;

import com.DevArena.backend.battle.entity.RatingHistory;
import com.DevArena.backend.battle.repository.RatingHistoryRepository;
import com.DevArena.backend.user.entity.User;
import com.DevArena.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RatingService {

    private final UserRepository userRepository;
    private final RatingHistoryRepository historyRepository;
    private static final int K = 32;

    public void updateRatings(Long battleId, Long winnerId, Long loserId) {

        User winner = userRepository.findById(winnerId)
                .orElseThrow();

        User loser = userRepository.findById(loserId)
                .orElseThrow();

        int Ra = winner.getRating();
        int Rb = loser.getRating();

        double Ea = 1.0 / (1 + Math.pow(10, (Rb - Ra) / 400.0));
        double Eb = 1.0 / (1 + Math.pow(10, (Ra - Rb) / 400.0));

        int newRa = (int) (Ra + K * (1 - Ea));
        int newRb = (int) (Rb + K * (0 - Eb));

        int deltaWinner = newRa - Ra;
        int deltaLoser = newRb - Rb;
        winner.setRating(newRa);
        loser.setRating(newRb);
        winner.setRank(calculateRank(newRa));
        loser.setRank(calculateRank(newRb));

        userRepository.save(winner);
        userRepository.save(loser);

        historyRepository.save(
            RatingHistory.builder()
                .userId(winnerId)
                .battleId(battleId) 
                .oldRating(Ra)
                .newRating(newRa)
                .delta(deltaWinner)
                .timestamp(System.currentTimeMillis())
                .build()
        );
        
        historyRepository.save(
            RatingHistory.builder()
                .userId(loserId)
                .battleId(battleId)
                .oldRating(Rb)
                .newRating(newRb)
                .delta(deltaLoser)
                .timestamp(System.currentTimeMillis())
                .build()
        );

    }

    public void updateDraw(Long player1Id, Long player2Id) {

        User p1 = userRepository.findById(player1Id).orElseThrow();
        User p2 = userRepository.findById(player2Id).orElseThrow();

        int Ra = p1.getRating();
        int Rb = p2.getRating();

        double Ea = 1.0 / (1 + Math.pow(10, (Rb - Ra) / 400.0));
        double Eb = 1.0 / (1 + Math.pow(10, (Ra - Rb) / 400.0));

        int newRa = (int) (Ra + K * (0.5 - Ea));
        int newRb = (int) (Rb + K * (0.5 - Eb));

        p1.setRating(newRa);
        p2.setRating(newRb);
        p1.setRank(calculateRank(newRa));
        p2.setRank(calculateRank(newRb));

        userRepository.save(p1);
        userRepository.save(p2);
    }
    private String calculateRank(int rating) {
        if (rating < 1100) return "Bronze";
        if (rating < 1300) return "Silver";
        if (rating < 1600) return "Gold";
        if (rating < 2000) return "Platinum";
        return "Diamond";
    }
    
    public RatingHistory getRatingChangeForBattle(Long userId, Long battleId) {
        return historyRepository.findFirstByUserIdAndBattleIdOrderByTimestampDesc(userId, battleId).orElse(null);
    }
    
}
