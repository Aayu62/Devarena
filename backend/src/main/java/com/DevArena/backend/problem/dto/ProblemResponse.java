package com.DevArena.backend.problem.dto;

import com.DevArena.backend.common.enums.Difficulty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemResponse {

    private Long id;
    private String title;
    private String slug;
    private Difficulty difficulty;
    private String description;
    private String constraints;
    private String inputFormat;
    private String outputFormat;
    private String sampleInput;
    private String sampleOutput;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
}