package com.DevArena.backend.problem.entity;

import com.DevArena.backend.problem.enums.ParameterType;
import jakarta.persistence.*;
import lombok.*;

/**
 * One parameter of a FunctionSignature.
 *
 * Example for twoSum(int[] nums, int target):
 *   Row 1: name="nums",   type=INT_ARRAY, orderIndex=0
 *   Row 2: name="target", type=INT,       orderIndex=1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_id", nullable = false)
    private FunctionSignature signature;

    /** Parameter name as it appears in the starter code. */
    @Column(nullable = false)
    private String name;

    /** The type of this parameter. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParameterType type;

    /** 0-based position in the parameter list. */
    @Column(nullable = false)
    private Integer orderIndex;
}