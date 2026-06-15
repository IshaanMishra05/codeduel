package com.codeduel.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestCaseRequest {
    private String input;

    @NotBlank(message = "Expected output is required")
    private String expectedOutput;

    private Boolean isHidden = false;
}
