package com.codeduel.codeduel.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionEvent {
    private Long userId;
    private Long matchId;
    private String code;
    private String language;
}