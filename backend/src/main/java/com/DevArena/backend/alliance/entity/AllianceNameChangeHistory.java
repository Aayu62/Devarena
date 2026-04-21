package com.DevArena.backend.alliance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllianceNameChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long allianceId;

    private String oldName;

    private String newName;

    private Long changedByUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt = LocalDateTime.now();
}
