package com.DevArena.backend.battle.service;

import com.DevArena.backend.battle.dto.BattleEvent;
import com.DevArena.backend.battle.entity.*;
import com.DevArena.backend.battle.repository.*;
import com.DevArena.backend.problem.repository.ProblemRepository;
import com.DevArena.backend.user.entity.User;
import com.DevArena.backend.problem.entity.Problem;
import com.DevArena.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import com.DevArena.backend.common.enums.Difficulty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final MatchmakingRepository queueRepo;
    private final BattleRepository battleRepo;
    private final UserService userService;
    private final ProblemRepository problemRepo;
    private static final long DIFFICULTY_RELAX_TIME = 60_000; // 60 sec
    private final SimpMessagingTemplate messagingTemplate;


    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processQueue() {
    
        List<MatchmakingEntry> queue = queueRepo.findAllByOrderByRatingAsc();
        long now = System.currentTimeMillis();
    
        for (int i = 0; i < queue.size(); i++) {
    
            MatchmakingEntry p1 = queue.get(i);
    
            for (int j = i + 1; j < queue.size(); j++) {
    
                MatchmakingEntry p2 = queue.get(j);
    
                long wait1 = now - p1.getJoinedAt();
                long wait2 = now - p2.getJoinedAt();
    
                int allowed1 = calculateAllowedDiff(wait1);
                int allowed2 = calculateAllowedDiff(wait2);
    
                int ratingDiff = Math.abs(p1.getRating() - p2.getRating());
    
                if (ratingDiff > Math.min(allowed1, allowed2)) {
                    continue;
                }
    
                Difficulty d1 = p1.getDifficulty();
                Difficulty d2 = p2.getDifficulty();
    
                boolean relaxDifficulty =
                        wait1 >= DIFFICULTY_RELAX_TIME ||
                        wait2 >= DIFFICULTY_RELAX_TIME;
    
                // If both selected specific difficulty and they differ
                if (d1 != null && d2 != null && !d1.equals(d2)) {
                    if (!relaxDifficulty) {
                        continue;
                    }
                }
    
                // ✅ Select problem
                List<Problem> problems;
    
                if (relaxDifficulty) {
                    problems = problemRepo.findByActiveTrue();
                } else {
                    if (d1 != null) {
                        problems = problemRepo
                               .findByDifficultyAndActiveTrue(d1,
                                       org.springframework.data.domain.PageRequest.of(0, 50))
                               .getContent();
                    } else if (d2 != null) {
                        problems = problemRepo
                               .findByDifficultyAndActiveTrue(d2,
                                       org.springframework.data.domain.PageRequest.of(0, 50))
                               .getContent();
                    } else {
                        problems = problemRepo
                               .findByActiveTrue(
                                       org.springframework.data.domain.PageRequest.of(0, 100))
                               .getContent();
                    }
                }
    
                if (problems.isEmpty()) {
                    continue;
                }
    
                Problem randomProblem =
                        problems.get(new java.util.Random().nextInt(problems.size()));
    
                // ✅ Create battle
                Battle battle = new Battle();
                battle.setPlayer1(p1.getUserId());
                battle.setPlayer2(p2.getUserId());
                battle.setProblemId(randomProblem.getId());
                battle.setStatus(BattleStatus.ACTIVE);
                battle.setStartedAt(System.currentTimeMillis());
                battle.setType(BattleType.RANKED);
    
                battleRepo.save(battle);

                messagingTemplate.convertAndSend(
                    "/topic/battle/" + battle.getId(),
                    new BattleEvent(
                        "STARTED",
                        battle.getId(),
                        null,
                        "Battle has started!"
                    )
                );
                
    
                queueRepo.delete(p1);
                queueRepo.delete(p2);
    
                return; // one match per cycle
            }
        }
    }
    
    @Transactional
    public Battle joinQueue(Difficulty difficulty) {
    
        User me = userService.currentUser();
    
        if (battleRepo.existsByPlayer1AndStatus(me.getId(), BattleStatus.ACTIVE)
                || battleRepo.existsByPlayer2AndStatus(me.getId(), BattleStatus.ACTIVE)) {
    
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Finish your current active battle first."
            );
        }
    
        if (queueRepo.existsByUserId(me.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You are already in matchmaking queue."
            );
        }
    
        MatchmakingEntry entry = new MatchmakingEntry();
        entry.setUserId(me.getId());
        entry.setRating(me.getRating());
        entry.setJoinedAt(System.currentTimeMillis());
        entry.setDifficulty(difficulty);
    
        queueRepo.save(entry);
    
        return null; // waiting
    }


    @Transactional
    public void leaveQueue() {
        User me = userService.currentUser();
        queueRepo.findByUserId(me.getId())
                .ifPresent(queueRepo::delete);
    }

    private int calculateAllowedDiff(long waitTime) {
        if (waitTime < 30_000) return 100;
        if (waitTime < 60_000) return 200;
        if (waitTime < 120_000) return 400;
        return Integer.MAX_VALUE;
    }
    
}
