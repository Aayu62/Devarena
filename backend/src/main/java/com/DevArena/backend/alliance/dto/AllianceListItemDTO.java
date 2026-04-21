package com.DevArena.backend.alliance.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllianceListItemDTO {

    private Long id;
    private String name;
    private String description;
    private String leaderUsername;
    private Integer memberCount;
    private Integer maxMembers;
    private String recentMessagePreview;
    private Boolean userIsMember;
    private Boolean userCanJoin;
}
