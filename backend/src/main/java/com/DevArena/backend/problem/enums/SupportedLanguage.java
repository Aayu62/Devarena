package com.DevArena.backend.problem.enums;

/**
 * Languages supported at launch, with their Judge0 language IDs.
 * Adding a new language later only requires:
 *   1. Adding an entry here
 *   2. Adding generation logic in DriverGeneratorService
 */
public enum SupportedLanguage {

    PYTHON(71,      "Python 3",     ".py"),
    JAVA(62,        "Java",         ".java"),
    CPP(54,         "C++ (GCC 17)", ".cpp"),
    JAVASCRIPT(63,  "JavaScript",   ".js");

    private final int judge0Id;
    private final String displayName;
    private final String extension;

    SupportedLanguage(int judge0Id, String displayName, String extension) {
        this.judge0Id    = judge0Id;
        this.displayName = displayName;
        this.extension   = extension;
    }

    public int getJudge0Id()       { return judge0Id; }
    public String getDisplayName() { return displayName; }
    public String getExtension()   { return extension; }

    /** Resolve from a Judge0 language ID. Returns null if not supported. */
    public static SupportedLanguage fromJudge0Id(int id) {
        for (SupportedLanguage lang : values()) {
            if (lang.judge0Id == id) return lang;
        }
        return null;
    }
}