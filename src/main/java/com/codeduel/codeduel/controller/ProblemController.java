package com.codeduel.codeduel.controller;

import com.codeduel.codeduel.dto.request.ProblemRequest;
import com.codeduel.codeduel.dto.response.ProblemResponse;
import com.codeduel.codeduel.entity.Problem;
import com.codeduel.codeduel.repository.ProblemRepository;
import com.codeduel.codeduel.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ProblemController.java
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public ResponseEntity<List<ProblemResponse>> getAll() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProblemResponse> create(@Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(problemService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        problemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}