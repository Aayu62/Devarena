package com.DevArena.backend.common.converter;

import com.DevArena.backend.common.enums.Difficulty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToDifficultyConverter implements Converter<String, Difficulty> {
    @Override
    public Difficulty convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return Difficulty.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid difficulty: " + source + ". Valid values are: EASY, MEDIUM, HARD"
            );
        }
    }
}
