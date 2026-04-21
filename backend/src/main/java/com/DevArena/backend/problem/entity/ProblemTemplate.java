package com.DevArena.backend.problem.entity;

import com.DevArena.backend.problem.enums.SupportedLanguage;
import jakarta.persistence.*;
import lombok.*;

/**
 * Starter code shown to the player in the editor.
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
    name = "problem_template",
    uniqueConstraints = @UniqueConstraint(columnNames = {"problem_id", "language"})
)
public class ProblemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportedLanguage language;

    /** The starter code shown in the player's editor. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String starterCode;
}