package com.codeduel.codeduel.service;

import com.codeduel.codeduel.dto.request.TestCaseRequest;
import com.codeduel.codeduel.dto.response.TestCaseResponse;
import com.codeduel.codeduel.entity.Problem;
import com.codeduel.codeduel.entity.TestCase;
import com.codeduel.codeduel.repository.ProblemRepository;
import com.codeduel.codeduel.repository.TestCaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestCaseService {
    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public TestCaseService(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    public List<TestCaseResponse> getAllTestCases() {
        return testCaseRepository.findAll()
                .stream().map(this::toTestCaseResponse)
                .toList();
    }

    public TestCaseResponse getById(long id) {
        return testCaseRepository.findById(id)
                .map(this::toTestCaseResponse)
                .orElseThrow(() -> new RuntimeException("Test Case not found"));
    }

    public List<TestCaseResponse> getByProblemId(Long problemId) {
        return testCaseRepository.findByProblemId(problemId)
                .stream()
                .map(this::toTestCaseResponse)
                .toList();
    }

    public TestCaseResponse create(TestCaseRequest testCaseRequest) {
        TestCase testCase = new TestCase();
        testCase.setInput(testCaseRequest.getInput());
        testCase.setExpectedOutput(testCaseRequest.getExpectedOutput());
        testCase.setIsHidden(testCaseRequest.getIsHidden());

        Problem problem = problemRepository.findById(testCaseRequest.getProblemId())
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        testCase.setProblem(problem);

        testCaseRepository.save(testCase);
        return toTestCaseResponse(testCase);
    }

    public void delete(Long id) {
        testCaseRepository.deleteById(id);
    }

    private TestCaseResponse toTestCaseResponse(TestCase testCase) {
        TestCaseResponse response = new TestCaseResponse();
        response.setId(testCase.getId());
        response.setInput(testCase.getInput());
        response.setExpectedOutput(testCase.getExpectedOutput());
        response.setIsHidden(testCase.getIsHidden());
        return response;
    }
}
