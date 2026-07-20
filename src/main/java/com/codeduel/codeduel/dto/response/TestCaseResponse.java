package com.codeduel.codeduel.dto.response;

import lombok.Data;

@Data
public class TestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private Boolean isHidden;
}
