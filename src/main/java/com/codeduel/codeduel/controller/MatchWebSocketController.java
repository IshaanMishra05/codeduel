package com.codeduel.codeduel.controller;

import com.codeduel.codeduel.dto.websocket.MatchEvent;
import com.codeduel.codeduel.dto.websocket.SubmissionEvent;
import com.codeduel.codeduel.dto.websocket.SubmissionResult;
import com.codeduel.codeduel.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MatchWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MatchService matchService;

    @MessageMapping("/match/{matchId}/ready")
    public void playerReady(
            @DestinationVariable Long matchId,
            @Payload MatchEvent event) {

        System.out.println(">>> playerReady called, matchId=" + matchId + ", userId=" + event.getUserId());

        matchService.handlePlayerReady(matchId, event.getUserId());

        boolean bothReady = matchService.areBothPlayersReady(matchId);

        if (bothReady) {
            matchService.startMatch(matchId);
            messagingTemplate.convertAndSend(
                    "/topic/match/" + matchId,
                    new MatchEvent("MATCH_STARTED", matchId, null, null, null, null)
            );
        } else {
            messagingTemplate.convertAndSend(
                    "/topic/match/" + matchId,
                    new MatchEvent("PLAYER_READY", matchId, event.getUserId(), null, null, null)
            );
        }
    }

    @MessageMapping("/match/{matchId}/submit")
    public void submitCode(
            @DestinationVariable Long matchId,
            @Payload SubmissionEvent event) {

        System.out.println(">>> submitCode called, matchId=" + matchId + ", userId=" + event.getUserId());

        // Step 1: Run the code against test cases
        SubmissionResult result = matchService.processSubmission(matchId, event);

        // Step 2: Broadcast result to both players
        messagingTemplate.convertAndSend(
                "/topic/match/" + matchId,
                new MatchEvent("SUBMISSION_RESULT", matchId, event.getUserId(),
                        result.getTestsPassed(), result.getTotalTests(), result.getStatus())
        );


        // Step 3: If all tests passed → end the match
        if (result.isAllPassed()) {
            matchService.finishMatch(matchId, event.getUserId());
            messagingTemplate.convertAndSend(
                    "/topic/match/" + matchId,
                    new MatchEvent("MATCH_FINISHED", matchId, event.getUserId(), null, null, null)
            );
        }
    }
}