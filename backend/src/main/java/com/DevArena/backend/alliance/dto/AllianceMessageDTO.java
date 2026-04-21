package com.DevArena.backend.alliance.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllianceMessageDTO {

    private Long id;
    private Long allianceId;
    private Long userId;
    private String username;
    private String userRank;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEdited;
}
