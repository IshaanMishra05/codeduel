package com.codeduel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchPlayerResponse {
    private Long userId;
    private String username;
    private Integer eloRating;
    private Boolean isReady;
    private Integer finalEloChange;
}
