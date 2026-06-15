package com.codeduel.websocket;

import com.codeduel.dto.response.SubmissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around SimpMessagingTemplate.
 * Services call this to push real-time events without knowing about STOMP details.
 */
@Component
@RequiredArgsConstructor
public class MatchEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String MATCH_TOPIC = "/topic/match/";

    public void publishPlayerJoined(String roomCode, String username, int eloRating, int playerCount) {
        MatchEvent.PlayerJoined event = MatchEvent.PlayerJoined.builder()
                .type(MatchEvent.Type.PLAYER_JOINED)
                .username(username)
                .eloRating(eloRating)
                .playerCount(playerCount)
                .build();
        messagingTemplate.convertAndSend(MATCH_TOPIC + roomCode, event);
    }

    public void publishPlayerReady(String roomCode, String username, boolean allReady) {
        MatchEvent.PlayerReady event = MatchEvent.PlayerReady.builder()
                .type(MatchEvent.Type.PLAYER_READY)
                .username(username)
                .allReady(allReady)
                .build();
        messagingTemplate.convertAndSend(MATCH_TOPIC + roomCode, event);
    }

    public void publishSubmissionResult(String roomCode, SubmissionResponse submission) {
        MatchEvent.SubmissionUpdate event = MatchEvent.SubmissionUpdate.builder()
                .type(MatchEvent.Type.SUBMISSION_RESULT)
                .submission(submission)
                .build();
        messagingTemplate.convertAndSend(MATCH_TOPIC + roomCode + "/progress", event);
    }

    public void publishMatchFinished(String roomCode, String winnerUsername,
                                      int winnerDelta, int loserDelta) {
        MatchEvent.MatchFinished event = MatchEvent.MatchFinished.builder()
                .type(MatchEvent.Type.MATCH_FINISHED)
                .winnerUsername(winnerUsername)
                .winnerEloDelta(winnerDelta)
                .loserEloDelta(loserDelta)
                .build();
        messagingTemplate.convertAndSend(MATCH_TOPIC + roomCode, event);
    }
}
