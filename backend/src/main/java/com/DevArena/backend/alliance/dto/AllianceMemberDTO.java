package com.DevArena.backend.alliance.dto;

import com.DevArena.backend.alliance.entity.AllianceRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllianceMemberDTO {

    private Long id;
    private Long userId;
    private Long allianceId;
    private String username;
    private String rank;
    private Integer rating;
    private String college;
    private AllianceRole role;
    private LocalDateTime joinedAt;
    private Boolean isOnline;
}
