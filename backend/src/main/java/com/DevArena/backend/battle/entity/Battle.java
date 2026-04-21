package com.DevArena.backend.battle.entity;

import com.DevArena.backend.common.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long player1;
    private Long player2;
    private Long problemId;
    private Long startedAt;
    private Long winnerId;

    @Enumerated(EnumType.STRING)
    private BattleStatus status;

    @Enumerated(EnumType.STRING)
    private BattleType type;

    /**
     * Friendly battles only.
     * A short alphanumeric code (e.g. "A3F9K2") that the creator shares with their friend.
     * Null for RANKED battles — those are matched by the queue, no code needed.
     */
    @Column(unique = true, nullable = true, length = 10)
    private String joinCode;

    /**
     * The difficulty tier chosen when creating a friendly battle.
     * For RANKED battles the difficulty comes from the matchmaking queue entry.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Difficulty difficulty;
}