package com.DevArena.backend.battle.service;

import com.DevArena.backend.battle.dto.*;
import com.DevArena.backend.battle.entity.*;
import com.DevArena.backend.battle.repository.*;
import com.DevArena.backend.common.enums.Difficulty;
import com.DevArena.backend.problem.dto.ProblemResponse;
import com.DevArena.backend.problem.entity.*;
import com.DevArena.backend.problem.repository.*;
import com.DevArena.backend.problem.service.*;
import com.DevArena.backend.user.dto.ProfileStats;
import com.DevArena.backend.user.entity.User;
import com.DevArena.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final BattleRepository      battleRepo;
    private final ProblemRepository     problemRepo;
    private final SubmissionRepository  subRepo;
    private final UserService           userService;
    private final Judge0Service         judge0;
    private final TestCaseRepository    testCaseRepo;
    private final ProblemService        problemService;
    private final RatingService         ratingService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final long BATTLE_DURATION = 15 * 60 * 1000;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int    CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void ensureUserNotInActiveBattle(User me) {
        if (battleRepo.existsByPlayer1AndStatus(me.getId(), BattleStatus.ACTIVE)
                || battleRepo.existsByPlayer2AndStatus(me.getId(), BattleStatus.ACTIVE)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Finish your current active battle first.");
        }
    }

    private String generateUniqueJoinCode() {
        String code;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
            if (++attempts > 20) throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate a unique join code.");
        } while (battleRepo.findByJoinCode(code).isPresent());
        return code;
    }

    private Problem chooseProblem(Difficulty difficulty) {
        List<Problem> pool;
        if (difficulty == null || difficulty == Difficulty.RANDOM) {
            pool = problemRepo.findByActiveTrue();
        } else {
            pool = problemRepo.findByDifficultyAndActiveTrue(
                    difficulty,
                    org.springframework.data.domain.PageRequest.of(0, 200)
            ).getContent();
            if (pool.isEmpty()) pool = problemRepo.findByActiveTrue();
        }
        if (pool.isEmpty()) throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "No active problems available.");
        return pool.get(RANDOM.nextInt(pool.size()));
    }

    /**
     * Combines the player's code with the hidden driver before sending to Judge0.
     * The driver reads stdin, calls the player's function, and prints the result.
     */
    private String buildFullCode(String userCode, Long problemId, int languageId) {
        String driverCode = problemService.getDriverCode(problemId, languageId);
        return userCode + "\n\n" + driverCode;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FRIENDLY BATTLE — CREATE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public FriendlyBattleResponse createFriendly(FriendlyBattleRequest req) {
        User me = userService.currentUser();
        ensureUserNotInActiveBattle(me);

        Problem problem;
        if (req.problemId() != null) {
            problem = problemRepo.findById(req.problemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
            if (!problem.getActive()) throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "That problem is not active");
        } else {
            problem = chooseProblem(req.difficulty());
        }

        String joinCode = generateUniqueJoinCode();
        Battle b = new Battle();
        b.setPlayer1(me.getId());
        b.setStatus(BattleStatus.WAITING);
        b.setType(BattleType.FRIEND);
        b.setProblemId(problem.getId());
        b.setJoinCode(joinCode);
        b.setDifficulty(req.difficulty() != null ? req.difficulty() : Difficulty.RANDOM);
        battleRepo.save(b);

        return new FriendlyBattleResponse(
                b.getId(), joinCode, b.getStatus(), b.getType(), b.getDifficulty(), b.getProblemId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FRIENDLY BATTLE — JOIN BY CODE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Battle joinByCode(String joinCode) {
        User me = userService.currentUser();
        ensureUserNotInActiveBattle(me);

        Battle b = battleRepo.findByJoinCode(joinCode.toUpperCase().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No battle found with that code"));

        if (b.getStatus() != BattleStatus.WAITING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This battle has already started or been cancelled");
        if (b.getPlayer1().equals(me.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot join your own battle");
        if (b.getPlayer2() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Battle is already full");

        b.setPlayer2(me.getId());
        b.setStatus(BattleStatus.ACTIVE);
        b.setStartedAt(System.currentTimeMillis());
        return battleRepo.save(b);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PROBLEM FETCH
    // ─────────────────────────────────────────────────────────────────────────

    public ProblemResponse getBattleProblem(Long battleId) {
        Battle battle = battleRepo.findById(battleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Battle not found"));
        return problemService.getProblemById(battle.getProblemId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RUN  (debug — visible + custom only, hidden never touched)
    //  Driver is prepended so the player's function is called correctly.
    // ─────────────────────────────────────────────────────────────────────────

    public List<RunResult> run(Long problemId, String code, Integer languageId,
                               List<RunRequest.CustomTestCase> customCases) {
        try {
            // Build full executable code = user function + hidden driver
            String fullCode = buildFullCode(code, problemId, languageId);

            var visibleCases = testCaseRepo
                    .findByProblemIdAndHiddenFalseOrderByOrderIndexAsc(problemId);

            if (visibleCases.isEmpty())
                return List.of(new RunResult("(none)", "(none)", "", "No example test case", "Example"));

            List<RunResult> results = new java.util.ArrayList<>();

            // Example test cases
            for (TestCase tc : visibleCases) {
                Judge0Service.JudgeResult res = judge0.run(fullCode, tc.getInput(), languageId);
                if (res == null) {
                    results.add(new RunResult(tc.getInput(), tc.getExpectedOutput(), "", "Judge error", "Example"));
                    continue;
                }
                String actual   = res.stdout() == null ? "" : res.stdout().trim();
                String expected = tc.getExpectedOutput() == null ? "" : tc.getExpectedOutput().trim();
                results.add(new RunResult(tc.getInput(), expected, actual,
                        expected.equals(actual) ? "Accepted" : "Wrong Answer", "Example"));
            }

            // Player's custom test cases
            if (customCases != null) {
                int idx = 1;
                for (RunRequest.CustomTestCase cc : customCases) {
                    String label = "Custom " + idx++;
                    Judge0Service.JudgeResult res = judge0.run(fullCode, cc.input(), languageId);
                    if (res == null) {
                        results.add(new RunResult(cc.input(), cc.expected(), "", "Judge error", label));
                        continue;
                    }
                    String actual   = res.stdout() == null ? "" : res.stdout().trim();
                    String expected = cc.expected() == null ? "" : cc.expected().trim();
                    results.add(new RunResult(cc.input(), expected, actual,
                            expected.equals(actual) ? "Accepted" : "Wrong Answer", label));
                }
            }

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(new RunResult("", "", "", "Error: " + e.getMessage(), "Error"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SUBMIT
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public SubmitResult submit(Long battleId, String code, Integer languageId) {
        try {
            User me = userService.currentUser();
            Battle battle = battleRepo.findById(battleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Battle not found"));

            if (!battle.getPlayer1().equals(me.getId()) &&
                    (battle.getPlayer2() == null || !battle.getPlayer2().equals(me.getId())))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this battle");

            if (battle.getStatus() != BattleStatus.ACTIVE)
                return new SubmitResult(false, "Battle is not active", null, null);

            if (System.currentTimeMillis() - battle.getStartedAt() > BATTLE_DURATION) {
                finishBattleInternal(battle);
                return new SubmitResult(false, "Time expired — battle has ended", null, null);
            }

            Submission old = subRepo.findTopByBattleIdAndUserIdOrderBySubmittedAtDesc(battleId, me.getId());
            if (old != null && System.currentTimeMillis() - old.getSubmittedAt() < 10_000)
                return new SubmitResult(false, "Cooldown active — wait 10 seconds", null, null);

            // Build full code (user function + driver) — used for ALL judge calls
            String fullCode = buildFullCode(code, battle.getProblemId(), languageId);

            // Visible test cases
            var visibleCases = testCaseRepo.findByProblemIdAndHiddenFalseOrderByOrderIndexAsc(battle.getProblemId());
            int passedVisible = 0, totalVisible = visibleCases.size();
            if (totalVisible > 0) {
                List<String> inputs = visibleCases.stream().map(TestCase::getInput).toList();
                List<Judge0Service.JudgeResult> res = judge0.runBatch(fullCode, inputs, languageId);
                for (int i = 0; i < visibleCases.size(); i++) {
                    var tc = visibleCases.get(i);
                    var r = (res != null && i < res.size()) ? res.get(i) : null;
                    if (r != null && "Accepted".equals(r.status()) && r.stdout() != null
                            && r.stdout().trim().equals(tc.getExpectedOutput().trim())) passedVisible++;
                }
            }

            // Hidden test cases
            var hiddenCases = testCaseRepo.findByProblemIdAndHiddenTrueOrderByOrderIndexAsc(battle.getProblemId());
            int passedHidden = 0, totalHidden = hiddenCases.size();
            if (totalHidden > 0) {
                List<String> inputs = hiddenCases.stream().map(TestCase::getInput).toList();
                List<Judge0Service.JudgeResult> res = judge0.runBatch(fullCode, inputs, languageId);
                for (int i = 0; i < hiddenCases.size(); i++) {
                    var tc = hiddenCases.get(i);
                    var r = (res != null && i < res.size()) ? res.get(i) : null;
                    if (r != null && "Accepted".equals(r.status()) && r.stdout() != null
                            && r.stdout().trim().equals(tc.getExpectedOutput().trim())) passedHidden++;
                }
            }

            if (old != null) subRepo.delete(old);

            Submission s = new Submission();
            s.setBattleId(battleId);
            s.setUserId(me.getId());
            s.setCode(code);          // store original user code, not the combined code
            s.setPassedVisible(passedVisible);
            s.setTotalVisible(totalVisible);
            s.setPassedHidden(passedHidden);
            s.setTotalHidden(totalHidden);
            s.setFullyEvaluated(true);
            s.setSubmittedAt(System.currentTimeMillis());
            s.setLanguageId(languageId);
            subRepo.save(s);

            try {
                messagingTemplate.convertAndSend("/topic/battle/" + battleId,
                        new BattleEvent("SUBMITTED", battleId, me.getId(), "Player submitted"));
            } catch (Exception e) {
                System.err.println("WebSocket notify error: " + e.getMessage());
            }

            Long opponentId = battle.getPlayer1().equals(me.getId())
                    ? battle.getPlayer2() : battle.getPlayer1();

            if (opponentId != null) {
                Submission opponentSub = subRepo
                        .findTopByBattleIdAndUserIdOrderBySubmittedAtDesc(battleId, opponentId);
                if (opponentSub != null) {
                    finishBattleInternal(battle);
                    return new SubmitResult(true,
                            "Both players submitted — battle ended. Check /result for scores.",
                            passedVisible, totalVisible);
                }
            }

            return new SubmitResult(true, "Submission recorded. Waiting for opponent...",
                    passedVisible, totalVisible);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Submission failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  END BATTLE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public String endBattle(Long battleId) {
        try {
            User me = userService.currentUser();
            Battle battle = battleRepo.findByIdForUpdate(battleId).orElseThrow();

            if (!battle.getPlayer1().equals(me.getId()) &&
                    (battle.getPlayer2() == null || !battle.getPlayer2().equals(me.getId())))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this battle.");

            if (battle.getStatus() == BattleStatus.WAITING) {
                battle.setStatus(BattleStatus.CANCELLED);
                battleRepo.save(battle);
                return "Battle cancelled";
            }
            if (battle.getStatus() != BattleStatus.ACTIVE) return "Battle already finished";

            // Forfeit = automatic loss
            Long opponentId = battle.getPlayer1().equals(me.getId())
                    ? battle.getPlayer2() : battle.getPlayer1();

            battle.setStatus(BattleStatus.FINISHED);
            battle.setWinnerId(opponentId);
            
            if (battle.getType() == BattleType.RANKED && opponentId != null)
                ratingService.updateRatings(battleId, opponentId, me.getId());
            
            battleRepo.save(battle);
            broadcastFinished(battle);
            return "You forfeited. Opponent wins.";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in endBattle: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to end battle: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INTERNAL FINISH
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void finishBattleInternal(Battle battle) {
        battle = battleRepo.findByIdForUpdate(battle.getId()).orElseThrow();
        if (battle.getStatus() != BattleStatus.ACTIVE) return;

        battle.setStatus(BattleStatus.FINISHED);
        boolean isRanked = battle.getType() == BattleType.RANKED;

        Submission s1 = subRepo.findTopByBattleIdAndUserIdOrderBySubmittedAtDesc(
                battle.getId(), battle.getPlayer1());
        Submission s2 = battle.getPlayer2() != null
                ? subRepo.findTopByBattleIdAndUserIdOrderBySubmittedAtDesc(
                        battle.getId(), battle.getPlayer2()) : null;

        if (s1 == null && s2 == null) {
            battle.setWinnerId(null);
            if (isRanked) ratingService.updateDraw(battle.getPlayer1(), battle.getPlayer2());
            battleRepo.save(battle); broadcastFinished(battle); return;
        }
        if (s1 != null && s2 == null) {
            battle.setWinnerId(battle.getPlayer1());
            if (isRanked) ratingService.updateRatings(battle.getId(), battle.getPlayer1(), battle.getPlayer2());
            battleRepo.save(battle); broadcastFinished(battle); return;
        }
        if (s1 == null) {
            battle.setWinnerId(battle.getPlayer2());
            if (isRanked) ratingService.updateRatings(battle.getId(), battle.getPlayer2(), battle.getPlayer1());
            battleRepo.save(battle); broadcastFinished(battle); return;
        }

        int score1 = s1.totalScore(), score2 = s2.totalScore();
        Long winner, loser;

        if (score1 > score2)                              { winner = battle.getPlayer1(); loser = battle.getPlayer2(); }
        else if (score2 > score1)                         { winner = battle.getPlayer2(); loser = battle.getPlayer1(); }
        else if (s1.getSubmittedAt() < s2.getSubmittedAt()) { winner = battle.getPlayer1(); loser = battle.getPlayer2(); }
        else if (s2.getSubmittedAt() < s1.getSubmittedAt()) { winner = battle.getPlayer2(); loser = battle.getPlayer1(); }
        else {
            battle.setWinnerId(null);
            if (isRanked) ratingService.updateDraw(battle.getPlayer1(), battle.getPlayer2());
            battleRepo.save(battle); broadcastFinished(battle); return;
        }

        battle.setWinnerId(winner);
        if (isRanked) ratingService.updateRatings(battle.getId(), winner, loser);
        battleRepo.save(battle);
        broadcastFinished(battle);
    }

    private void broadcastFinished(Battle battle) {
        try {
            messagingTemplate.convertAndSend("/topic/battle/" + battle.getId(),
                    new BattleEvent("FINISHED", battle.getId(), battle.getWinnerId(), "Battle finished"));
        } catch (Exception e) {
            System.err.println("WebSocket broadcast error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RESULT
    // ─────────────────────────────────────────────────────────────────────────

    public BattleResult getResult(Long battleId) {
        Battle battle = battleRepo.findById(battleId).orElseThrow();

        if (battle.getStatus() == BattleStatus.ACTIVE && battle.getStartedAt() != null
                && System.currentTimeMillis() - battle.getStartedAt() > BATTLE_DURATION) {
            finishBattleInternal(battle);
            battle = battleRepo.findById(battleId).orElseThrow();
        }

        if (battle.getStatus() == BattleStatus.FINISHED) {
            Submission s1 = subRepo.findTopByBattleIdAndUserIdOrderBySubmittedAtDesc(
                    battleId, battle.getPlayer1());
            Submission s2 = battle.getPlayer2() != null
                    ? subRepo.findTopByBattleIdAndUserIdOrderBySubmittedAtDesc(
                            battleId, battle.getPlayer2()) : null;
            
            // Get usernames
            User p1 = userService.getUserById(battle.getPlayer1());
            User p2 = battle.getPlayer2() != null ? userService.getUserById(battle.getPlayer2()) : null;
            
            // Get rating changes from history (only for ranked battles)
            Integer p1RatingChange = null;
            Integer p2RatingChange = null;
            if (battle.getType() == BattleType.RANKED) {
                try {
                    RatingHistory p1History = ratingService.getRatingChangeForBattle(battle.getPlayer1(), battleId);
                    p1RatingChange = p1History != null ? p1History.getDelta() : 0;
                    
                    if (battle.getPlayer2() != null) {
                        RatingHistory p2History = ratingService.getRatingChangeForBattle(battle.getPlayer2(), battleId);
                        p2RatingChange = p2History != null ? p2History.getDelta() : 0;
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching rating history: " + e.getMessage());
                }
            }
            
            // Get problem title
            Problem problem = problemRepo.findById(battle.getProblemId()).orElse(null);
            String problemTitle = problem != null ? problem.getTitle() : "Unknown";
            
            // Calculate duration
            Long duration = battle.getStartedAt() != null 
                ? System.currentTimeMillis() - battle.getStartedAt() : null;
            
            return new BattleResult(
                battle.getStatus(), 
                battle.getType(),
                battle.getWinnerId(),
                battle.getPlayer1(), 
                battle.getPlayer2(),
                p1.getUsername(),
                p2 != null ? p2.getUsername() : null,
                s1 != null ? s1.totalScore() : 0,
                s1 != null ? s1.getTotalVisible() + s1.getTotalHidden() : 0,
                s2 != null ? s2.totalScore() : 0,
                s2 != null ? s2.getTotalVisible() + s2.getTotalHidden() : 0,
                p1RatingChange,
                p2RatingChange,
                problemTitle,
                duration
            );
        }

        return new BattleResult(battle.getStatus(), battle.getType(), null, 
            battle.getPlayer1(), battle.getPlayer2(), null, null, 
            null, null, null, null, null, null, null, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HISTORY / STATS
    // ─────────────────────────────────────────────────────────────────────────

    public List<BattleHistoryItem> getMyBattleHistory() {
        User me = userService.currentUser();
        return battleRepo.findByPlayer1OrPlayer2OrderByStartedAtDesc(me.getId(), me.getId())
                .stream().map(b -> {
                    Long opponent = b.getPlayer1().equals(me.getId()) ? b.getPlayer2() : b.getPlayer1();
                    String result;
                    if (b.getStatus() != BattleStatus.FINISHED) result = b.getStatus().name();
                    else if (b.getWinnerId() == null) result = "DRAW";
                    else if (b.getWinnerId().equals(me.getId())) result = "WIN";
                    else result = "LOSS";
                    return new BattleHistoryItem(b.getId(), b.getPlayer1(), b.getPlayer2(), opponent,
                            b.getType(), b.getStatus(), result, b.getWinnerId(), b.getStartedAt());
                }).toList();
    }

    public ProfileStats getMyStats() {
        User me = userService.currentUser();
        List<Battle> battles = battleRepo.findByPlayer1OrPlayer2OrderByStartedAtDesc(me.getId(), me.getId());
        long wins = 0, losses = 0, draws = 0;
        for (Battle b : battles) {
            if (b.getStatus() != BattleStatus.FINISHED || b.getType() != BattleType.RANKED) continue;
            if (b.getWinnerId() == null) draws++;
            else if (b.getWinnerId().equals(me.getId())) wins++;
            else losses++;
        }
        long total = wins + losses + draws;
        return new ProfileStats(total, wins, losses, draws, me.getRating(),
                total == 0 ? 0 : (wins * 100.0) / total);
    }
}