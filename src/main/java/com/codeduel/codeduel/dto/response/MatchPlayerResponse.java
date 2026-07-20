package com.codeduel.codeduel.dto.response;

import lombok.Data;

@Data
public class MatchPlayerResponse {
    private Long id;
    private String username;
    private Boolean isReady;
}
