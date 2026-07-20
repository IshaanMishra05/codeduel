package com.codeduel.codeduel.controller;

import com.codeduel.codeduel.dto.request.MatchRequest;
import com.codeduel.codeduel.dto.response.MatchResponse;
import com.codeduel.codeduel.entity.Match;
import com.codeduel.codeduel.repository.MatchRepository;
import com.codeduel.codeduel.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// MatchController.java
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<List<MatchResponse>> getAll() {
        return ResponseEntity.ok(matchService.findAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getById(id));
    }

    @GetMapping("/room/{roomCode}")
    public ResponseEntity<MatchResponse> getByRoomCode(@PathVariable String roomCode) {
        return ResponseEntity.ok(matchService.getByRoomCode(roomCode));
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(@RequestBody MatchRequest request) {
        return ResponseEntity.ok(matchService.create(request));
    }
}
