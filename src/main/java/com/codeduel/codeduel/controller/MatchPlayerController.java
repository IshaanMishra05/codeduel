package com.codeduel.codeduel.controller;

import com.codeduel.codeduel.dto.request.MatchPlayerRequest;
import com.codeduel.codeduel.dto.response.MatchPlayerResponse;
import com.codeduel.codeduel.service.MatchPlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matchplayers")
public class MatchPlayerController {

    private final MatchPlayerService matchPlayerService;

    public MatchPlayerController(MatchPlayerService matchPlayerService) {
        this.matchPlayerService = matchPlayerService;
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MatchPlayerResponse>> getAllByMatchId(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchPlayerService.getByMatchId(matchId));
    }

    @PostMapping
    public ResponseEntity<MatchPlayerResponse> createMatchPlayer(@RequestBody MatchPlayerRequest matchPlayerRequest) {
        return ResponseEntity.ok(matchPlayerService.join(matchPlayerRequest));
    }
}
