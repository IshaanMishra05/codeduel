package com.codeduel.controller;

import com.codeduel.dto.request.SubmissionRequest;
import com.codeduel.dto.response.SubmissionResponse;
import com.codeduel.entity.User;
import com.codeduel.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal User user) {
        SubmissionResponse response = submissionService.submit(
                request.getMatchId(), request.getCode(), request.getLanguage(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            @PathVariable Long matchId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.getSubmissionsForMatch(matchId, user.getId()));
    }
}
