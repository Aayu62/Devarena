package com.DevArena.backend.battle.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long battleId;
    private Long userId;

    @Column(length = 10000)
    private String code;

    private Integer passedVisible;
    private Integer totalVisible;
    private Integer passedHidden;
    private Integer totalHidden;
    
    @Column(nullable = false)
    private boolean fullyEvaluated = false;

    private Long submittedAt; // timestamp

    private Integer languageId; // Judge0 language id
    
    public int totalScore() {
        return (passedVisible == null ? 0 : passedVisible) +
               (passedHidden == null ? 0 : passedHidden);
    }
    
    public void markFullyEvaluated(int passedHidden, int totalHidden) {
        this.passedHidden = passedHidden;
        this.totalHidden = totalHidden;
        this.fullyEvaluated = true;
    }
}
