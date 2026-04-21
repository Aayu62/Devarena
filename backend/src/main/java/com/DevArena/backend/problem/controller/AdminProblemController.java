package com.DevArena.backend.problem.controller;

import com.DevArena.backend.problem.dto.*;
import com.DevArena.backend.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProblemController {

    private final ProblemService problemService;

    // ── Problem CRUD ─────────────────────────────────────────────────────────

    @PostMapping
    public ProblemResponse createProblem(@RequestBody ProblemCreateRequest request) {
        return problemService.createProblem(request);
    }

    @PostMapping("/{id}/testcases")
    public void addTestCase(@PathVariable Long id, @RequestBody TestCaseRequest request) {
        problemService.addTestCase(id, request);
    }

    @PatchMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        problemService.activateProblem(id);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        problemService.deactivateProblem(id);
    }

    // ── Function Signature ───────────────────────────────────────────────────

    /**
     * POST /admin/problems/{id}/signature
     *
     * Defines the function signature and auto-generates starter + driver code
     * for all 4 supported languages (Python, Java, C++, JavaScript).
     *
     * Body example:
     * {
     *   "functionName": "twoSum",
     *   "returnType": "INT_ARRAY",
     *   "parameters": [
     *     { "name": "nums",   "type": "INT_ARRAY", "orderIndex": 0 },
     *     { "name": "target", "type": "INT",       "orderIndex": 1 }
     *   ]
     * }
     *
     * Calling this again on the same problem replaces the existing signature
     * and regenerates all code.
     */
    @PostMapping("/{id}/signature")
    public GeneratedCodeResponse defineSignature(
            @PathVariable Long id,
            @RequestBody FunctionSignatureRequest request) {
        return problemService.defineSignature(id, request);
    }

    /**
     * GET /admin/problems/{id}/generated-code
     *
     * Preview all auto-generated starter code and driver code.
     * Use this to review before activating the problem.
     */
    @GetMapping("/{id}/generated-code")
    public GeneratedCodeResponse getGeneratedCode(@PathVariable Long id) {
        return problemService.getGeneratedCode(id);
    }

    /**
     * PUT /admin/problems/{id}/template/{languageId}
     *
     * Override the auto-generated starter code for a specific language.
     * languageId = Judge0 language ID (71=Python, 62=Java, 54=C++, 63=JS)
     *
     * Body: { "code": "def twoSum(nums, target):\n    pass\n" }
     */
    @PutMapping("/{id}/template/{languageId}")
    public void overrideTemplate(
            @PathVariable Long id,
            @PathVariable int languageId,
            @RequestBody OverrideCodeRequest request) {
        problemService.overrideTemplate(id, languageId, request.code());
    }

    /**
     * PUT /admin/problems/{id}/driver/{languageId}
     *
     * Override the auto-generated driver code for a specific language.
     * languageId = Judge0 language ID (71=Python, 62=Java, 54=C++, 63=JS)
     *
     * Body: { "code": "nums = list(map(int, input().split()))\n..." }
     */
    @PutMapping("/{id}/driver/{languageId}")
    public void overrideDriver(
            @PathVariable Long id,
            @PathVariable int languageId,
            @RequestBody OverrideCodeRequest request) {
        problemService.overrideDriver(id, languageId, request.code());
    }
}