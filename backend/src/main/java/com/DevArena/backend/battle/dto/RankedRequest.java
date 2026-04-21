package com.DevArena.backend.battle.dto;

import com.DevArena.backend.common.enums.Difficulty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankedRequest {
    private Difficulty difficulty;
}
