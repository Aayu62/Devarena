package com.DevArena.backend.alliance.dto;

import com.DevArena.backend.alliance.entity.AllianceRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllianceDetailDTO {

    private Long id;
    private String name;
    private String description;
    private Long leaderId;
    private String leaderUsername;
    private Integer maxMembers;
    private Integer memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AllianceRole currentUserRole;
    private Integer nameChangeCount;
    private LocalDateTime nextNameChangeAt;
}
