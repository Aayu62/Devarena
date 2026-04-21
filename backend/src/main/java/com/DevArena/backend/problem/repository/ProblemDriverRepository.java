package com.DevArena.backend.problem.repository;

import com.DevArena.backend.problem.entity.ProblemDriver;
import com.DevArena.backend.problem.enums.SupportedLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemDriverRepository extends JpaRepository<ProblemDriver, Long> {
    Optional<ProblemDriver> findByProblemIdAndLanguage(Long problemId, SupportedLanguage language);
    List<ProblemDriver> findAllByProblemId(Long problemId);
    void deleteAllByProblemId(Long problemId);
}