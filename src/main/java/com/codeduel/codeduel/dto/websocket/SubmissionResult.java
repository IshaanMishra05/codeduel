package com.codeduel.codeduel.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class SubmissionResult {
    private int testsPassed;
    private int totalTests;
    private boolean allPassed;
    private String status;
    private Long executionMs;
}