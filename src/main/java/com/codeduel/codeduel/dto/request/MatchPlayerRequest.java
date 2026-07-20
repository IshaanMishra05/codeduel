package com.codeduel.codeduel.dto.request;

import lombok.Data;

@Data
public class MatchPlayerRequest {
    private Long matchId;
    private Long userId;
}