package com.codeduel.codeduel.service;

import com.codeduel.codeduel.dto.request.ProblemRequest;
import com.codeduel.codeduel.dto.response.ProblemResponse;
import com.codeduel.codeduel.entity.Problem;
import com.codeduel.codeduel.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;
    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public List<ProblemResponse> getAllProblems() {
        return problemRepository.findAll()
                .stream().map(this::toProblemResponse)
                .toList();
    }

    public ProblemResponse getById(long id) {
        return problemRepository.findById(id)
                .map(this::toProblemResponse)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
    }

    public ProblemResponse create(ProblemRequest problemRequest) {
        Problem problem = new Problem();
        problem.setTitle(problemRequest.getTitle());
        problem.setDescription(problemRequest.getDescription());
        problem.setLanguage(problemRequest.getLanguage());
        problem.setDifficulty(problemRequest.getDifficulty());
        problemRepository.save(problem);
        return toProblemResponse(problem);
    }

    public void delete(Long id) {
        problemRepository.deleteById(id);
    }

    private ProblemResponse toProblemResponse(Problem problem) {
        ProblemResponse response = new ProblemResponse();
        response.setId(problem.getId());
        response.setTitle(problem.getTitle());
        response.setDescription(problem.getDescription());
        response.setLanguage(problem.getLanguage());
        response.setDifficulty(problem.getDifficulty());
        response.setCreatedBy(problem.getCreatedBy() != null ? problem.getCreatedBy().getUsername() : null);
        return response;
    }
}
