package com.codeduel.controller;

import com.codeduel.dto.request.ProblemRequest;
import com.codeduel.dto.response.ProblemResponse;
import com.codeduel.entity.User;
import com.codeduel.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    // ── Public-ish: any authenticated user can browse problems ──────────────

    @GetMapping
    public ResponseEntity<List<ProblemResponse>> getAll(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(problemService.getAllProblems(difficulty, language));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblem(id));
    }

    // ── Admin-only mutations ─────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> create(
            @Valid @RequestBody ProblemRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(problemService.createProblem(request, admin));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProblemRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(problemService.updateProblem(id, request, admin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }

    // Admin variant — returns all test cases including hidden ones
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemResponse> getByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblemAdmin(id));
    }
}
