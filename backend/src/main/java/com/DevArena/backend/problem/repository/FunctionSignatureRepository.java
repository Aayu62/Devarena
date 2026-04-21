package com.DevArena.backend.problem.repository;

import com.DevArena.backend.problem.entity.FunctionSignature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FunctionSignatureRepository extends JpaRepository<FunctionSignature, Long> {
    Optional<FunctionSignature> findByProblemId(Long problemId);
    boolean existsByProblemId(Long problemId);
}