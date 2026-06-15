package com.codeduel.service;

import com.codeduel.dto.response.MatchPlayerResponse;
import com.codeduel.dto.response.MatchResponse;
import com.codeduel.entity.*;
import com.codeduel.exception.BadRequestException;
import com.codeduel.exception.ResourceNotFoundException;
import com.codeduel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final ProblemRepository problemRepository;

    // ------------------------------------------------------------------ //
    //  Room creation
    // ------------------------------------------------------------------ //

    @Transactional
    public MatchResponse createRoom(Long problemId, Integer timeLimitSeconds, User creator) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + problemId));

        Match match = Match.builder()
                .roomCode(generateUniqueRoomCode())
                .problem(problem)
                .status(MatchStatus.WAITING)
                .timeLimitSeconds(timeLimitSeconds != null ? timeLimitSeconds : 1800)
                .build();

        match = matchRepository.save(match);

        // Creator auto-joins
        MatchPlayer player = MatchPlayer.builder()
                .match(match)
                .user(creator)
                .isReady(false)
                .build();
        matchPlayerRepository.save(player);

        return toResponse(match);
    }

    // ------------------------------------------------------------------ //
    //  Joining
    // ------------------------------------------------------------------ //

    @Transactional
    public MatchResponse joinRoom(String roomCode, User user) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomCode));

        if (match.getStatus() != MatchStatus.WAITING) {
            throw new BadRequestException("This room is no longer accepting players");
        }

        long playerCount = matchPlayerRepository.countByMatchId(match.getId());
        if (playerCount >= 2) {
            throw new BadRequestException("Room is full");
        }

        if (matchPlayerRepository.existsByMatchIdAndUserId(match.getId(), user.getId())) {
            // Already in — just return current state (idempotent)
            return toResponse(match);
        }

        MatchPlayer player = MatchPlayer.builder()
                .match(match)
                .user(user)
                .isReady(false)
                .build();
        matchPlayerRepository.save(player);

        return toResponse(match);
    }

    // ------------------------------------------------------------------ //
    //  Ready up
    // ------------------------------------------------------------------ //

    @Transactional
    public MatchResponse setReady(String roomCode, User user) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomCode));

        MatchPlayer mp = matchPlayerRepository.findByMatchIdAndUserId(match.getId(), user.getId())
                .orElseThrow(() -> new BadRequestException("You are not in this room"));

        mp.setIsReady(true);
        matchPlayerRepository.save(mp);

        // If both players are ready → start the match
        List<MatchPlayer> players = matchPlayerRepository.findByMatchId(match.getId());
        boolean allReady = players.size() == 2 && players.stream().allMatch(p -> Boolean.TRUE.equals(p.getIsReady()));

        if (allReady) {
            match.setStatus(MatchStatus.ACTIVE);
            match.setStartedAt(LocalDateTime.now());
            match = matchRepository.save(match);
        }

        return toResponse(match);
    }

    // ------------------------------------------------------------------ //
    //  Query helpers
    // ------------------------------------------------------------------ //

    @Transactional(readOnly = true)
    public MatchResponse getByRoomCode(String roomCode) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomCode));
        return toResponse(match);
    }

    @Transactional(readOnly = true)
    public MatchResponse getById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + id));
        return toResponse(match);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getOpenRooms() {
        return matchRepository.findByStatus(MatchStatus.WAITING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Internal: finish match (called by SubmissionService)
    // ------------------------------------------------------------------ //

    @Transactional
    public Match finishMatch(Match match, User winner) {
        match.setStatus(MatchStatus.FINISHED);
        match.setEndedAt(LocalDateTime.now());
        match.setWinner(winner);
        return matchRepository.save(match);
    }

    @Transactional(readOnly = true)
    public Match findMatchEntity(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private String generateUniqueRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no ambiguous chars
        Random rng = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
            code = sb.toString();
        } while (matchRepository.existsByRoomCode(code));
        return code;
    }

    public MatchResponse toResponse(Match match) {
        List<MatchPlayer> players = matchPlayerRepository.findByMatchId(match.getId());

        List<MatchPlayerResponse> playerResponses = players.stream()
                .map(mp -> MatchPlayerResponse.builder()
                        .userId(mp.getUser().getId())
                        .username(mp.getUser().getUsername())
                        .eloRating(mp.getUser().getEloRating())
                        .isReady(mp.getIsReady())
                        .finalEloChange(mp.getFinalEloChange())
                        .build())
                .collect(Collectors.toList());

        return MatchResponse.builder()
                .id(match.getId())
                .roomCode(match.getRoomCode())
                .problemId(match.getProblem() != null ? match.getProblem().getId() : null)
                .problemTitle(match.getProblem() != null ? match.getProblem().getTitle() : null)
                .status(match.getStatus())
                .startedAt(match.getStartedAt())
                .endedAt(match.getEndedAt())
                .timeLimitSeconds(match.getTimeLimitSeconds())
                .winnerUsername(match.getWinner() != null ? match.getWinner().getUsername() : null)
                .players(playerResponses)
                .createdAt(match.getCreatedAt())
                .build();
    }
}
