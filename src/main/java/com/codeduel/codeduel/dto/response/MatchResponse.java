package com.codeduel.codeduel.dto.response;

import lombok.Data;

@Data
public class MatchResponse {
    private Long id;
    private String roomCode;
    private Long problemId;
    private String problemTitle;
    private String status;
}
