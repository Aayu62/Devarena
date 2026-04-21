package com.DevArena.backend.problem.repository;

import com.DevArena.backend.problem.entity.Problem;
import com.DevArena.backend.common.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);

    Page<Problem> findByActiveTrue(Pageable pageable);
    List<Problem> findByActiveTrue();

    Page<Problem> findByDifficultyAndActiveTrue(Difficulty difficulty, Pageable pageable);
}