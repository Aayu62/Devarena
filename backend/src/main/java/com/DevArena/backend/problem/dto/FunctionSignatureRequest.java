package com.DevArena.backend.problem.dto;

import com.DevArena.backend.problem.enums.ParameterType;
import lombok.Data;

import java.util.List;

/**
 * POST /admin/problems/{id}/signature
 *
 * Example for Two Sum:
 * {
 *   "functionName": "twoSum",
 *   "returnType": "INT_ARRAY",
 *   "parameters": [
 *     { "name": "nums",   "type": "INT_ARRAY", "orderIndex": 0 },
 *     { "name": "target", "type": "INT",       "orderIndex": 1 }
 *   ]
 * }
 */
@Data
public class FunctionSignatureRequest {
    private String functionName;
    private ParameterType returnType;
    private List<ParameterRequest> parameters;

    @Data
    public static class ParameterRequest {
        private String name;
        private ParameterType type;
        private Integer orderIndex;
    }
}