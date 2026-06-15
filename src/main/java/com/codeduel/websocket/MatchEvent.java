package com.codeduel.websocket;

import com.codeduel.dto.response.SubmissionResponse;
import lombok.Builder;
import lombok.Data;

/**
 * All real-time messages broadcast over STOMP.
 *
 * Clients subscribe to:
 *   /topic/match/{roomCode}          — match-wide events (join, ready, finish)
 *   /topic/match/{roomCode}/progress — live submission progress for both players
 */
public class MatchEvent {

    public enum Type {
        PLAYER_JOINED,
        PLAYER_READY,
        MATCH_STARTED,
        SUBMISSION_RESULT,
        MATCH_FINISHED
    }

    @Data
    @Builder
    public static class PlayerJoined {
        private Type type;
        private String username;
        private Integer eloRating;
        private int playerCount;
    }

    @Data
    @Builder
    public static class PlayerReady {
        private Type type;
        private String username;
        private boolean allReady;
    }

    @Data
    @Builder
    public static class SubmissionUpdate {
        private Type type;
        private SubmissionResponse submission;
    }

    @Data
    @Builder
    public static class MatchFinished {
        private Type type;
        private String winnerUsername;
        private int winnerEloDelta;
        private int loserEloDelta;
    }
}
