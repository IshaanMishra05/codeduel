package com.codeduel.codeduel.dto.response;

import lombok.Data;

@Data
public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String language;
    private String createdBy;
}