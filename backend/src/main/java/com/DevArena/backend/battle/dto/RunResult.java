package com.DevArena.backend.battle.dto;

/**
 * Result for a single test case during a Run (debug) execution.
 *
 * @param label    Human-readable label: "Example", "Custom 1", "Custom 2", etc.
 * @param input    The stdin that was sent.
 * @param expected The expected output.
 * @param output   The actual stdout from the judge.
 * @param verdict  "Accepted" or "Wrong Answer" or error description.
 */
public record RunResult(
        String input,
        String expected,
        String output,
        String verdict,
        String label
) {
    /** Backwards-compat constructor without label (used internally). */
    public RunResult(String input, String expected, String output, String verdict) {
        this(input, expected, output, verdict, "Example");
    }
}