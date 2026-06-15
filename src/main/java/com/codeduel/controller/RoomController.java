package com.codeduel.controller;

import com.codeduel.dto.request.RoomRequest;
import com.codeduel.dto.response.MatchResponse;
import com.codeduel.entity.User;
import com.codeduel.service.MatchService;
import com.codeduel.websocket.MatchEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final MatchService matchService;
    private final MatchEventPublisher matchEventPublisher;

    @PostMapping
    public ResponseEntity<MatchResponse> createRoom(
            @Valid @RequestBody RoomRequest.Create request,
            @AuthenticationPrincipal User user) {
        MatchResponse response = matchService.createRoom(
                request.getProblemId(), request.getTimeLimitSeconds(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/join")
    public ResponseEntity<MatchResponse> joinRoom(
            @Valid @RequestBody RoomRequest.Join request,
            @AuthenticationPrincipal User user) {
        MatchResponse response = matchService.joinRoom(request.getRoomCode(), user);

        // Notify other player via WebSocket
        matchEventPublisher.publishPlayerJoined(
                request.getRoomCode(),
                user.getUsername(),
                user.getEloRating(),
                response.getPlayers().size()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomCode}/ready")
    public ResponseEntity<MatchResponse> readyUp(
            @PathVariable String roomCode,
            @AuthenticationPrincipal User user) {
        MatchResponse response = matchService.setReady(roomCode, user);

        boolean allReady = response.getPlayers().stream()
                .allMatch(p -> Boolean.TRUE.equals(p.getIsReady()));
        matchEventPublisher.publishPlayerReady(roomCode, user.getUsername(), allReady);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<MatchResponse> getRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(matchService.getByRoomCode(roomCode));
    }

    @GetMapping
    public ResponseEntity<List<MatchResponse>> getOpenRooms() {
        return ResponseEntity.ok(matchService.getOpenRooms());
    }
}
