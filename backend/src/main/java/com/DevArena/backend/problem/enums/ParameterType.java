package com.DevArena.backend.problem.enums;

/**
 * All supported parameter/return types for function signatures.
 * These map to concrete types in each supported language.
 *
 * Stdin encoding (how test case input is written):
 *   INT          → single integer on its own line          e.g. "9"
 *   INT_ARRAY    → space-separated integers on one line    e.g. "2 7 11 15"
 *   INT_MATRIX   → n lines, each with space-separated ints (first line = n)
 *   STRING       → single string on its own line           e.g. "hello"
 *   STRING_ARRAY → count on first line, then one per line
 *   BOOL         → "true" or "false"
 *   FLOAT        → single float                            e.g. "3.14"
 *   FLOAT_ARRAY  → space-separated floats on one line
 */
public enum ParameterType {
    INT,
    INT_ARRAY,
    INT_MATRIX,
    STRING,
    STRING_ARRAY,
    BOOL,
    FLOAT,
    FLOAT_ARRAY
}