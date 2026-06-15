package com.codeduel.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubmissionResponse {
    private Long id;
    private Long matchId;
    private Long userId;
    private String username;
    private String language;
    private String status;
    private Integer testsPassed;
    private Integer totalTests;
    private Long executionMs;
    private String compilerOutput;
    private LocalDateTime submittedAt;
}
