package com.codeduel.codeduel.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchEvent {
    private String type;
    private Long matchId;
    private Long userId;
    private Integer testsPassed;
    private Integer totalTests;
    private String status;
}