package com.DevArena.backend.battle.controller;

import com.DevArena.backend.battle.dto.*;
import com.DevArena.backend.battle.entity.Battle;
import com.DevArena.backend.battle.service.BattleService;
import com.DevArena.backend.battle.service.MatchmakingService;
import com.DevArena.backend.problem.dto.ProblemResponse;
import com.DevArena.backend.user.dto.ProfileStats;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/battles")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService service;
    private final MatchmakingService matchmakingService;

    // ── Stats ────────────────────────────────────────────────────────────────

    @GetMapping("/me/stats")
    public ProfileStats stats() { return service.getMyStats(); }

    // ── Problem ──────────────────────────────────────────────────────────────

    @GetMapping("/{battleId}/problem")
    public ProblemResponse getProblem(@PathVariable Long battleId) {
        return service.getBattleProblem(battleId);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FRIENDLY BATTLE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * POST /battles/friendly/create
     * Body: { "difficulty": "EASY" }
     *        or { "difficulty": "MEDIUM", "problemId": 5 }
     *        or { "difficulty": "RANDOM" }
     *
     * Returns a joinCode the creator shares with their friend.
     * Friendly battles never affect ratings.
     */
    @PostMapping("/friendly/create")
    public FriendlyBattleResponse createFriendly(@RequestBody FriendlyBattleRequest req) {
        return service.createFriendly(req);
    }

    /**
     * POST /battles/friendly/join
     * Body: { "joinCode": "A3F9K2" }
     *
     * Friend enters the code → battle becomes ACTIVE immediately.
     */
    @PostMapping("/friendly/join")
    public Battle joinByCode(@RequestBody JoinByCodeRequest req) {
        return service.joinByCode(req.joinCode());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RANKED BATTLE (matchmaking)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * POST /battles/ranked/join
     * Body: { "difficulty": "EASY" }   (or null for any difficulty)
     *
     * Adds user to the matchmaking queue.
     * Returns null — the actual battle appears in /battles/history once matched.
     */
    @PostMapping("/ranked/join")
    public Battle joinRankedQueue(@RequestBody RankedRequest request) {
        return matchmakingService.joinQueue(request.getDifficulty());
    }

    /**
     * POST /battles/ranked/leave
     * Removes user from the matchmaking queue.
     */
    @PostMapping("/ranked/leave")
    public String leaveRankedQueue() {
        matchmakingService.leaveQueue();
        return "Left matchmaking queue";
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SHARED (both battle types use these)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * POST /battles/run
     * Debug run — evaluates against visible + custom test cases only.
     * Hidden test cases are never touched here.
     */
    @PostMapping("/run")
    public List<RunResult> run(@RequestBody RunRequest req) {
        return service.run(req.problemId(), req.code(), req.languageId(), req.customTestCases());
    }

    /**
     * POST /battles/submit?battleId=1&languageId=71
     * Body: raw source code (Content-Type: text/plain)
     *
     * Evaluates all test cases internally. Only visible-test-case counts
     * are returned during the battle; full scores revealed via /result after FINISHED.
     */
    @PostMapping("/submit")
    public SubmitResult submit(
            @RequestParam Long battleId,
            @RequestParam Integer languageId,
            @RequestBody String code) {
        return service.submit(battleId, code, languageId);
    }

    /**
     * POST /battles/end?battleId=1
     * Forfeit an active battle, or cancel a WAITING friendly battle.
     */
    @PostMapping("/end")
    public String end(@RequestParam Long battleId) {
        return service.endBattle(battleId);
    }

    /**
     * GET /battles/{battleId}/result
     * Returns full scores (visible + hidden) only after battle is FINISHED.
     * During ACTIVE/WAITING all score fields are null.
     */
    @GetMapping("/{battleId}/result")
    public BattleResult result(@PathVariable Long battleId) {
        return service.getResult(battleId);
    }

    /**
     * GET /battles/history
     */
    @GetMapping("/history")
    public List<BattleHistoryItem> history() {
        return service.getMyBattleHistory();
    }
}