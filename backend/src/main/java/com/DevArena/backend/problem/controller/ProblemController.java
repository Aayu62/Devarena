package com.DevArena.backend.problem.controller;

import com.DevArena.backend.problem.dto.ProblemResponse;
import com.DevArena.backend.problem.dto.StarterCodeResponse;
import com.DevArena.backend.problem.service.ProblemService;
import com.DevArena.backend.common.enums.Difficulty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public Page<ProblemResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Difficulty difficulty) {
        return problemService.listProblems(page, size, difficulty);
    }

    @GetMapping("/{slug}")
    public ProblemResponse get(@PathVariable String slug) {
        return problemService.getProblemBySlug(slug);
    }

    /**
     * GET /problems/{slug}/starter?languageId=71
     *
     * Returns the starter code for the given language.
     * The battleground frontend calls this when a player opens the editor
     * or switches language — the editor is pre-filled with this code.
     *
     * Supported languageId values:
     *   71 = Python 3
     *   62 = Java
     *   54 = C++ (GCC 17)
     *   63 = JavaScript (Node)
     */
    @GetMapping("/{slug}/starter")
    public StarterCodeResponse getStarterCode(
            @PathVariable String slug,
            @RequestParam int languageId) {
        return problemService.getStarterCode(slug, languageId);
    }
}