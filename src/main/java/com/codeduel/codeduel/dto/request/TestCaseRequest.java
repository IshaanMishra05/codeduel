package com.codeduel.codeduel.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestCaseRequest {

    @NotBlank
    private Long problemId;

    @NotBlank
    private String input;

    @NotBlank
    private String expectedOutput;

    @NotBlank
    private Boolean isHidden;
}
