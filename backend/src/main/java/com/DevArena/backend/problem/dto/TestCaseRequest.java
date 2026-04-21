package com.DevArena.backend.problem.dto;

import lombok.Data;

@Data
public class TestCaseRequest {
    private String input;
    private String expectedOutput;
    private Boolean hidden;
    private Integer orderIndex;
}