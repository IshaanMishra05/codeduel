package com.codeduel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntry {
    private Integer rank;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Integer eloRating;
    private Integer wins;
    private Integer losses;
    private Integer totalMatches;
    private Double winRate;
}
