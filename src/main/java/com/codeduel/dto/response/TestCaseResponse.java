package com.codeduel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private Boolean isHidden;
}
