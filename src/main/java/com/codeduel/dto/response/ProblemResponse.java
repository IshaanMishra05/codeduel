package com.codeduel.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String language;
    private String starterCode;
    private String createdByUsername;
    private List<TestCaseResponse> visibleTestCases;  // isHidden = false only
    private LocalDateTime createdAt;
}
