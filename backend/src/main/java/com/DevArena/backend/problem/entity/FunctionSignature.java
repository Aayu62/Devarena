package com.DevArena.backend.problem.entity;

import com.DevArena.backend.problem.enums.ParameterType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the function signature for a problem.
 * One FunctionSignature per Problem.
 *
 * Example for Two Sum:
 *   functionName = "twoSum"
 *   returnType   = INT_ARRAY
 *   parameters   = [("nums", INT_ARRAY, 0), ("target", INT, 1)]
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Back-reference to the problem. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false, unique = true)
    private Problem problem;

    /** The name of the function players implement. e.g. "twoSum" */
    @Column(nullable = false)
    private String functionName;

    /** The return type of the function. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParameterType returnType;

    /** Parameters in order. Cascade so they're saved/deleted with the signature. */
    @OneToMany(
            mappedBy = "signature",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<FunctionParameter> parameters = new ArrayList<>();
}