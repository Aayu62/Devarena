package com.DevArena.backend.problem.repository;

import com.DevArena.backend.problem.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    List<TestCase> findByProblemIdOrderByOrderIndexAsc(Long problemId);

    List<TestCase> findByProblemIdAndHiddenFalseOrderByOrderIndexAsc(Long problemId);

    List<TestCase> findByProblemIdAndHiddenTrueOrderByOrderIndexAsc(Long problemId);
}