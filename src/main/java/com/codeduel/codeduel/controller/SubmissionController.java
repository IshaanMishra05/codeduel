package com.codeduel.codeduel.controller;

import com.codeduel.codeduel.entity.Submission;
import com.codeduel.codeduel.repository.SubmissionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// SubmissionController.java
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionRepository submissionRepository;

    public SubmissionController(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/match/{matchId}/user/{userId}")
    public List<Submission> getByMatchAndUser(@PathVariable Long matchId, @PathVariable Long userId) {
        return submissionRepository.findByMatchIdAndUserId(matchId, userId);
    }

    @PostMapping
    public Submission create(@RequestBody Submission submission) {
        return submissionRepository.save(submission);
    }
}