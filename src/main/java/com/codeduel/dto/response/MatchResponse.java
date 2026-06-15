package com.codeduel.dto.response;

import com.codeduel.entity.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchResponse {
    private Long id;
    private String roomCode;
    private Long problemId;
    private String problemTitle;
    private MatchStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer timeLimitSeconds;
    private String winnerUsername;
    private List<MatchPlayerResponse> players;
    private LocalDateTime createdAt;
}
