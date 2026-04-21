package com.DevArena.backend.battle.entity;

import com.DevArena.backend.common.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long userId;

    private Integer rating;

    private Long joinedAt;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
}
