package com.codeduel.service;

import com.codeduel.dto.request.ProblemRequest;
import com.codeduel.dto.request.TestCaseRequest;
import com.codeduel.dto.response.ProblemResponse;
import com.codeduel.dto.response.TestCaseResponse;
import com.codeduel.entity.Problem;
import com.codeduel.entity.TestCase;
import com.codeduel.entity.User;
import com.codeduel.exception.ResourceNotFoundException;
import com.codeduel.repository.ProblemRepository;
import com.codeduel.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    @Transactional
    public ProblemResponse createProblem(ProblemRequest request, User admin) {
        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .language(request.getLanguage())
                .starterCode(request.getStarterCode())
                .createdBy(admin)
                .build();

        problem = problemRepository.save(problem);

        // Save test cases if provided
        if (request.getTestCases() != null) {
            saveTestCases(problem, request.getTestCases());
        }

        return toResponse(problem, true);
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblem(Long id) {
        Problem problem = findById(id);
        return toResponse(problem, false); // players see only visible test cases
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblemAdmin(Long id) {
        Problem problem = findById(id);
        return toResponse(problem, true); // admins see all test cases
    }

    @Transactional(readOnly = true)
    public List<ProblemResponse> getAllProblems(String difficulty, String language) {
        List<Problem> problems;
        if (difficulty != null && language != null) {
            problems = problemRepository.findByDifficultyAndLanguage(difficulty, language);
        } else if (difficulty != null) {
            problems = problemRepository.findByDifficulty(difficulty);
        } else if (language != null) {
            problems = problemRepository.findByLanguage(language);
        } else {
            problems = problemRepository.findAll();
        }
        return problems.stream()
                .map(p -> toResponse(p, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProblemResponse updateProblem(Long id, ProblemRequest request, User admin) {
        Problem problem = findById(id);

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setLanguage(request.getLanguage());
        problem.setStarterCode(request.getStarterCode());

        problem = problemRepository.save(problem);

        // Replace test cases entirely on update
        if (request.getTestCases() != null) {
            testCaseRepository.deleteByProblemId(problem.getId());
            saveTestCases(problem, request.getTestCases());
        }

        return toResponse(problem, true);
    }

    @Transactional
    public void deleteProblem(Long id) {
        findById(id); // ensures 404 if missing
        testCaseRepository.deleteByProblemId(id);
        problemRepository.deleteById(id);
    }

    // ---- helpers ----

    public Problem findById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + id));
    }

    private void saveTestCases(Problem problem, List<TestCaseRequest> requests) {
        List<TestCase> testCases = requests.stream()
                .map(r -> TestCase.builder()
                        .problem(problem)
                        .input(r.getInput())
                        .expectedOutput(r.getExpectedOutput())
                        .isHidden(r.getIsHidden() != null ? r.getIsHidden() : false)
                        .build())
                .collect(Collectors.toList());
        testCaseRepository.saveAll(testCases);
    }

    private ProblemResponse toResponse(Problem problem, boolean includeHidden) {
        List<TestCase> testCases = testCaseRepository.findByProblemId(problem.getId());

        List<TestCaseResponse> tcResponses = testCases.stream()
                .filter(tc -> includeHidden || !Boolean.TRUE.equals(tc.getIsHidden()))
                .map(tc -> TestCaseResponse.builder()
                        .id(tc.getId())
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .isHidden(tc.getIsHidden())
                        .build())
                .collect(Collectors.toList());

        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .language(problem.getLanguage())
                .starterCode(problem.getStarterCode())
                .createdByUsername(problem.getCreatedBy() != null ? problem.getCreatedBy().getUsername() : null)
                .visibleTestCases(tcResponses)
                .createdAt(problem.getCreatedAt())
                .build();
    }
}
