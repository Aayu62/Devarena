package com.DevArena.backend.problem.service;

import com.DevArena.backend.common.enums.Difficulty;
import com.DevArena.backend.problem.dto.*;
import com.DevArena.backend.problem.entity.*;
import com.DevArena.backend.problem.enums.SupportedLanguage;
import com.DevArena.backend.problem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository          problemRepo;
    private final TestCaseRepository         testCaseRepo;
    private final FunctionSignatureRepository signatureRepo;
    private final ProblemTemplateRepository  templateRepo;
    private final ProblemDriverRepository    driverRepo;
    private final DriverGeneratorService     driverGenerator;

    // =========================================================================
    //  ADMIN — PROBLEM CRUD
    // =========================================================================

    @Transactional
    public ProblemResponse createProblem(ProblemCreateRequest req) {
        if (problemRepo.existsBySlug(req.getSlug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug already exists");
        }
        validateLimits(req.getTimeLimitMs(), req.getMemoryLimitMb());

        Problem problem = Problem.builder()
                .title(req.getTitle())
                .slug(req.getSlug())
                .difficulty(req.getDifficulty())
                .description(req.getDescription())
                .constraints(req.getConstraints())
                .inputFormat(req.getInputFormat())
                .outputFormat(req.getOutputFormat())
                .sampleInput(req.getSampleInput())
                .sampleOutput(req.getSampleOutput())
                .timeLimitMs(req.getTimeLimitMs())
                .memoryLimitMb(req.getMemoryLimitMb())
                .build();

        problemRepo.save(problem);
        return mapToResponse(problem, null);
    }

    @Transactional
    public void addTestCase(Long problemId, TestCaseRequest req) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));

        if (req.getOrderIndex() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderIndex is required");
        }

        TestCase testCase = TestCase.builder()
                .problem(problem)
                .input(req.getInput())
                .expectedOutput(req.getExpectedOutput())
                .hidden(req.getHidden() == null ? true : req.getHidden())
                .orderIndex(req.getOrderIndex())
                .build();

        testCaseRepo.save(testCase);
    }

    // =========================================================================
    //  ADMIN — FUNCTION SIGNATURE  (auto-generates templates + drivers)
    // =========================================================================

    /**
     * Defines or replaces the function signature for a problem.
     * Automatically generates starter code and driver code for all 4 supported languages.
     * If a signature already exists it is replaced along with all generated code.
     */
    @Transactional
    public GeneratedCodeResponse defineSignature(Long problemId, FunctionSignatureRequest req) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));

        // Remove old signature + generated code if it exists
        signatureRepo.findByProblemId(problemId).ifPresent(old -> {
            templateRepo.deleteAllByProblemId(problemId);
            driverRepo.deleteAllByProblemId(problemId);
            signatureRepo.delete(old);
        });

        // Build and save the new signature
        FunctionSignature sig = new FunctionSignature();
        sig.setProblem(problem);
        sig.setFunctionName(req.getFunctionName());
        sig.setReturnType(req.getReturnType());

        List<FunctionParameter> params = req.getParameters().stream().map(p -> {
            FunctionParameter fp = new FunctionParameter();
            fp.setSignature(sig);
            fp.setName(p.getName());
            fp.setType(p.getType());
            fp.setOrderIndex(p.getOrderIndex());
            return fp;
        }).collect(Collectors.toList());

        sig.setParameters(params);
        signatureRepo.save(sig);

        // Generate starter code + driver for every supported language
        List<GeneratedCodeResponse.LanguageCode> languageCodes = new ArrayList<>();

        for (SupportedLanguage lang : SupportedLanguage.values()) {
            String starterCode = driverGenerator.generateStarterCode(sig, lang);
            String driverCode  = driverGenerator.generateDriverCode(sig, lang);

            ProblemTemplate template = ProblemTemplate.builder()
                    .problem(problem)
                    .language(lang)
                    .starterCode(starterCode)
                    .build();
            templateRepo.save(template);

            ProblemDriver driver = ProblemDriver.builder()
                    .problem(problem)
                    .language(lang)
                    .driverCode(driverCode)
                    .build();
            driverRepo.save(driver);

            languageCodes.add(new GeneratedCodeResponse.LanguageCode(
                    lang, lang.getJudge0Id(), lang.getDisplayName(), starterCode, driverCode));
        }

        return new GeneratedCodeResponse(problemId, req.getFunctionName(), languageCodes);
    }

    /**
     * Preview generated starter + driver for all languages.
     * Useful for admin to review before publishing the problem.
     */
    public GeneratedCodeResponse getGeneratedCode(Long problemId) {
        FunctionSignature sig = signatureRepo.findByProblemId(problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No signature defined for this problem yet"));

        List<GeneratedCodeResponse.LanguageCode> codes = new ArrayList<>();
        for (SupportedLanguage lang : SupportedLanguage.values()) {
            String starter = templateRepo.findByProblemIdAndLanguage(problemId, lang)
                    .map(ProblemTemplate::getStarterCode).orElse("(not generated)");
            String driver = driverRepo.findByProblemIdAndLanguage(problemId, lang)
                    .map(ProblemDriver::getDriverCode).orElse("(not generated)");
            codes.add(new GeneratedCodeResponse.LanguageCode(
                    lang, lang.getJudge0Id(), lang.getDisplayName(), starter, driver));
        }
        return new GeneratedCodeResponse(problemId, sig.getFunctionName(), codes);
    }

    /**
     * Admin override: replace the auto-generated starter code for a specific language.
     * Use the Judge0 language ID (71=Python, 62=Java, 54=C++, 63=JS).
     */
    @Transactional
    public void overrideTemplate(Long problemId, int judge0Id, String code) {
        SupportedLanguage lang = resolveLang(judge0Id);
        ProblemTemplate template = templateRepo.findByProblemIdAndLanguage(problemId, lang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Template not found — define the signature first"));
        template.setStarterCode(code);
        templateRepo.save(template);
    }

    /**
     * Admin override: replace the auto-generated driver code for a specific language.
     */
    @Transactional
    public void overrideDriver(Long problemId, int judge0Id, String code) {
        SupportedLanguage lang = resolveLang(judge0Id);
        ProblemDriver driver = driverRepo.findByProblemIdAndLanguage(problemId, lang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Driver not found — define the signature first"));
        driver.setDriverCode(code);
        driverRepo.save(driver);
    }

    // =========================================================================
    //  ADMIN — ACTIVATE / DEACTIVATE
    // =========================================================================

    @Transactional
    public void activateProblem(Long id) {
        Problem problem = problemRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));

        if (Boolean.TRUE.equals(problem.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem is already active");
        }

        // Must have a function signature
        if (!signatureRepo.existsByProblemId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot activate: define the function signature first");
        }

        // Must have at least one template + driver (at least one language supported)
        if (templateRepo.findAllByProblemId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot activate: no language templates generated");
        }

        List<TestCase> testCases = testCaseRepo.findByProblemIdOrderByOrderIndexAsc(id);
        if (testCases.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot activate: add test cases first");
        }
        if (testCases.stream().noneMatch(tc -> !tc.getHidden())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot activate: at least one visible test case is required");
        }
        if (testCases.stream().noneMatch(TestCase::getHidden)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot activate: at least one hidden test case is required");
        }

        problem.setActive(true);
        problemRepo.save(problem);
    }

    @Transactional
    public void deactivateProblem(Long id) {
        Problem problem = problemRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
        problem.setActive(false);
        problemRepo.save(problem);
    }

    // =========================================================================
    //  PUBLIC — PROBLEM LIST / DETAIL
    // =========================================================================

    public Page<ProblemResponse> listProblems(int page, int size, Difficulty difficulty) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Problem> problems = (difficulty != null)
                ? problemRepo.findByDifficultyAndActiveTrue(difficulty, pageable)
                : problemRepo.findByActiveTrue(pageable);
        return problems.map(p -> mapToResponse(p, null));
    }

    public ProblemResponse getProblemById(Long id) {
        Problem problem = problemRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
        if (!problem.getActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem is not active");
        }
        return mapToResponse(problem, null);
    }

    public ProblemResponse getProblemBySlug(String slug) {
        Problem problem = problemRepo.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
        return mapToResponse(problem, null);
    }

    // =========================================================================
    //  PUBLIC — STARTER CODE  (called by battleground per language)
    // =========================================================================

    /**
     * Returns the starter code for a given problem and Judge0 language ID.
     * The battleground calls this when the player selects a language.
     */
    public StarterCodeResponse getStarterCode(String slug, int judge0Id) {
        Problem problem = problemRepo.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));

        SupportedLanguage lang = resolveLang(judge0Id);

        ProblemTemplate template = templateRepo
                .findByProblemIdAndLanguage(problem.getId(), lang)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No starter code for language " + lang.getDisplayName()));

        return new StarterCodeResponse(problem.getId(), lang, lang.getJudge0Id(), template.getStarterCode());
    }

    // =========================================================================
    //  INTERNAL — used by BattleService to prepend driver before Judge0
    // =========================================================================

    /**
     * Fetches the hidden driver code for a problem + language.
     * BattleService appends this to the player's code before sending to Judge0.
     */
    public String getDriverCode(Long problemId, int judge0Id) {
        SupportedLanguage lang = resolveLang(judge0Id);
        return driverRepo.findByProblemIdAndLanguage(problemId, lang)
                .map(ProblemDriver::getDriverCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No driver found for language " + lang.getDisplayName() +
                        ". Make sure the problem has a function signature defined."));
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private SupportedLanguage resolveLang(int judge0Id) {
        SupportedLanguage lang = SupportedLanguage.fromJudge0Id(judge0Id);
        if (lang == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Language ID " + judge0Id + " is not supported. Supported: Python(71), Java(62), C++(54), JS(63)");
        }
        return lang;
    }

    private void validateLimits(Integer time, Integer memory) {
        if (time == null || time <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time limit");
        if (memory == null || memory <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid memory limit");
    }

    private ProblemResponse mapToResponse(Problem problem, String starterCode) {
        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .slug(problem.getSlug())
                .difficulty(problem.getDifficulty())
                .description(problem.getDescription())
                .constraints(problem.getConstraints())
                .inputFormat(problem.getInputFormat())
                .outputFormat(problem.getOutputFormat())
                .sampleInput(problem.getSampleInput())
                .sampleOutput(problem.getSampleOutput())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .build();
    }
}