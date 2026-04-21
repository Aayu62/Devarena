package com.DevArena.backend.battle.dto;

/**
 * Response to a Submit action.
 *
 * During an active battle, passedVisible/totalVisible are shown
 * (example test case counts only) so the player knows their code
 * compiles and runs — but hidden test case results are withheld
 * until the battle ends.
 *
 * Full scores (visible + hidden) are revealed via GET /{battleId}/result
 * once the battle status is FINISHED.
 */
public record SubmitResult(
        boolean success,
        String message,
        Integer passedVisible,   // null while battle is active (hidden)
        Integer totalVisible     // null while battle is active (hidden)
) {}