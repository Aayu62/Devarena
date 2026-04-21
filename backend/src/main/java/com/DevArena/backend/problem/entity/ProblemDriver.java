package com.DevArena.backend.problem.entity;

import com.DevArena.backend.problem.enums.SupportedLanguage;
import jakarta.persistence.*;
import lombok.*;

/**
 * Hidden driver code that is APPENDED to the player's code before sending to Judge0.
 * The player never sees this. It reads stdin, calls the player's function, prints the result.
 *
 * Auto-generated when admin defines the function signature.
 * Admin can override it via the admin API.
 *
 * One row per (problem, language) pair.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "problem_driver",
    uniqueConstraints = @UniqueConstraint(columnNames = {"problem_id", "language"})
)
public class ProblemDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportedLanguage language;

    /** The hidden driver code appended to user code before Judge0 execution. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String driverCode;
}