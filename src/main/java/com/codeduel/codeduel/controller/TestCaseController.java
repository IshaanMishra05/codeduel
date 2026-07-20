package com.codeduel.codeduel.controller;

import com.codeduel.codeduel.dto.request.TestCaseRequest;
import com.codeduel.codeduel.dto.response.TestCaseResponse;
import com.codeduel.codeduel.service.TestCaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/test-cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @GetMapping
    public ResponseEntity<List<TestCaseResponse>> getAll() {
        return ResponseEntity.ok(testCaseService.getAllTestCases());
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<TestCaseResponse>> getByProblemId(@PathVariable Long problemId) {
        return ResponseEntity.ok(testCaseService.getByProblemId(problemId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCaseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(testCaseService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TestCaseResponse> create(@Valid @RequestBody TestCaseRequest request) {
        return ResponseEntity.ok(testCaseService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        testCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}