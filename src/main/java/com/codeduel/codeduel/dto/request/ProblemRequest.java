package com.codeduel.codeduel.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProblemRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String difficulty;

    @NotBlank
    private String language;
}