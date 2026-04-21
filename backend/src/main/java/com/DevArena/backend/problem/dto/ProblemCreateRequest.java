package com.DevArena.backend.problem.dto;

import com.DevArena.backend.common.enums.Difficulty;
import lombok.Data;

@Data
public class ProblemCreateRequest {

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