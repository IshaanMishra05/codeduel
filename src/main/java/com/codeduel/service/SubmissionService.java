package com.codeduel.service;

import com.codeduel.dto.response.SubmissionResponse;
import com.codeduel.entity.*;
import com.codeduel.exception.BadRequestException;
import com.codeduel.exception.ResourceNotFoundException;
import com.codeduel.repository.*;
import com.codeduel.websocket.MatchEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final MatchRepository matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final CodeExecutionService codeExecutionService;
    private final EloService eloService;
    private final MatchService matchService;
    private final MatchEventPublisher matchEventPublisher;

    /**
     * Full submission pipeline:
     *   1. Validate match state
     *   2. Execute code against all test cases (including hidden)
     *   3. Persist submission
     *   4. If ACCEPTED → finish match, update ELO, broadcast result
     *   5. Broadcast live progress to both players either way
     */
    @Transactional
    public SubmissionResponse submit(Long matchId, String code, String language, User submitter) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));

        if (match.getStatus() != MatchStatus.ACTIVE) {
            throw new BadRequestException("Match is not active (status: " + match.getStatus() + ")");
        }

        if (!matchPlayerRepository.existsByMatchIdAndUserId(matchId, submitter.getId())) {
            throw new BadRequestException("You are not a participant of this match");
        }

        // Load ALL test cases (visible + hidden) for execution
        List<TestCase> testCases = testCaseRepository.findByProblemId(match.getProblem().getId());
        if (testCases.isEmpty()) {
            throw new BadRequestException("Problem has no test cases");
        }

        // Run code
        CodeExecutionService.ExecutionResult execResult =
                codeExecutionService.execute(code, language, testCases);

        // Persist
        Submission submission = Submission.builder()
                .match(match)
                .user(submitter)
                .code(code)
                .language(language)
                .status(execResult.status())
                .testsPassed(execResult.testsPassed())
                .totalTests(execResult.totalTests())
                .executionMs(execResult.executionMs())
                .compilerOutput(execResult.compilerOutput())
                .build();

        submission = submissionRepository.save(submission);

        // Broadcast real-time update to the match room
        matchEventPublisher.publishSubmissionResult(match.getRoomCode(), toResponse(submission));

        // If first ACCEPTED in this match → this player wins
        if ("ACCEPTED".equals(execResult.status()) && match.getStatus() == MatchStatus.ACTIVE) {
            finalizeMatch(match, submitter);
        }

        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsForMatch(Long matchId, Long userId) {
        return submissionRepository.findByMatchIdAndUserId(matchId, userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Match finalization: update ELO, mark winner, broadcast
    // ------------------------------------------------------------------ //

    private void finalizeMatch(Match match, User winner) {
        List<MatchPlayer> players = matchPlayerRepository.findByMatchId(match.getId());
        if (players.size() != 2) return;

        MatchPlayer winnerMp = players.stream()
                .filter(mp -> mp.getUser().getId().equals(winner.getId()))
                .findFirst().orElseThrow();
        MatchPlayer loserMp = players.stream()
                .filter(mp -> !mp.getUser().getId().equals(winner.getId()))
                .findFirst().orElseThrow();

        User loser = loserMp.getUser();

        // Calculate Elo deltas
        int[] deltas = eloService.calculateDeltas(
                winner.getEloRating(), loser.getEloRating(),
                winner.getTotalMatches(), loser.getTotalMatches()
        );
        int winnerDelta = deltas[0];
        int loserDelta  = deltas[1];

        // Update winner stats
        winner.setEloRating(winner.getEloRating() + winnerDelta);
        winner.setWins(winner.getWins() + 1);
        winner.setTotalMatches(winner.getTotalMatches() + 1);
        userRepository.save(winner);

        // Update loser stats
        loser.setEloRating(Math.max(100, loser.getEloRating() + loserDelta)); // floor at 100
        loser.setLosses(loser.getLosses() + 1);
        loser.setTotalMatches(loser.getTotalMatches() + 1);
        userRepository.save(loser);

        // Record Elo change on the join table
        winnerMp.setFinalEloChange(winnerDelta);
        loserMp.setFinalEloChange(loserDelta);
        matchPlayerRepository.save(winnerMp);
        matchPlayerRepository.save(loserMp);

        // Mark match finished
        matchService.finishMatch(match, winner);

        // Broadcast match-over event
        matchEventPublisher.publishMatchFinished(match.getRoomCode(),
                winner.getUsername(), winnerDelta, loserDelta);

        log.info("Match {} finished. Winner: {} (+{}). Loser: {} ({}).",
                match.getRoomCode(), winner.getUsername(), winnerDelta,
                loser.getUsername(), loserDelta);
    }

    // ------------------------------------------------------------------ //
    //  Mapping
    // ------------------------------------------------------------------ //

    private SubmissionResponse toResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .matchId(s.getMatch().getId())
                .userId(s.getUser().getId())
                .username(s.getUser().getUsername())
                .language(s.getLanguage())
                .status(s.getStatus())
                .testsPassed(s.getTestsPassed())
                .totalTests(s.getTotalTests())
                .executionMs(s.getExecutionMs())
                .compilerOutput(s.getCompilerOutput())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
