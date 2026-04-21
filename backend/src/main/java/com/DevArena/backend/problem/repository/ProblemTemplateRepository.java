package com.DevArena.backend.problem.repository;

import com.DevArena.backend.problem.entity.ProblemTemplate;
import com.DevArena.backend.problem.enums.SupportedLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemTemplateRepository extends JpaRepository<ProblemTemplate, Long> {
    Optional<ProblemTemplate> findByProblemIdAndLanguage(Long problemId, SupportedLanguage language);
    List<ProblemTemplate> findAllByProblemId(Long problemId);
    void deleteAllByProblemId(Long problemId);
}