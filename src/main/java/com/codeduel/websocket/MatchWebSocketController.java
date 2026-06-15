package com.codeduel.websocket;

import com.codeduel.entity.User;
import com.codeduel.repository.UserRepository;
import com.codeduel.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * Handles messages sent FROM the client over WebSocket.
 *
 * Client sends:
 *   /app/match/{roomCode}/ready      — mark yourself ready
 *
 * Server broadcasts to:
 *   /topic/match/{roomCode}          — all match events
 */
@Controller
@RequiredArgsConstructor
public class MatchWebSocketController {

    private final MatchService matchService;
    private final MatchEventPublisher matchEventPublisher;
    private final UserRepository userRepository;

    /**
     * Called when a player clicks "Ready" in the waiting room.
     * The HTTP REST endpoint (POST /api/rooms/{code}/ready) also exists,
     * so clients can use whichever is convenient.
     */
    @MessageMapping("/match/{roomCode}/ready")
    public void playerReady(@DestinationVariable String roomCode,
                             Authentication authentication) {
        User user = resolveUser(authentication);
        var matchResponse = matchService.setReady(roomCode, user);

        boolean allReady = matchResponse.getPlayers().stream()
                .allMatch(p -> Boolean.TRUE.equals(p.getIsReady()));

        matchEventPublisher.publishPlayerReady(roomCode, user.getUsername(), allReady);
    }

    private User resolveUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }
}
