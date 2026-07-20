package com.codeduel.codeduel.service;

import com.codeduel.codeduel.dto.request.MatchRequest;
import com.codeduel.codeduel.dto.response.MatchResponse;
import com.codeduel.codeduel.dto.websocket.SubmissionEvent;
import com.codeduel.codeduel.dto.websocket.SubmissionResult;
import com.codeduel.codeduel.entity.*;
import com.codeduel.codeduel.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final ProblemRepository problemRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final CodeExecutionService codeExecutionService;

    public MatchService(MatchRepository matchRepository, ProblemRepository problemRepository, MatchPlayerRepository matchPlayerRepository, TestCaseRepository testCaseRepository, UserRepository userRepository, SubmissionRepository submissionRepository, UserService userService, CodeExecutionService codeExecutionService) {
        this.matchRepository = matchRepository;
        this.problemRepository = problemRepository;
        this.matchPlayerRepository = matchPlayerRepository;
        this.testCaseRepository = testCaseRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
        this.codeExecutionService = codeExecutionService;
    }

    public List<MatchResponse> findAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(this::toMatchResponse)
                .toList();
    }

    public MatchResponse create(MatchRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        Match match = new Match();
        match.setProblem(problem);
        match.setRoomCode(generateRoomCode());
        match.setStatus(MatchStatus.WAITING);
        matchRepository.save(match);
        return toMatchResponse(match);
    }

    public MatchResponse getById(Long id) {
        return matchRepository.findById(id)
                .map(this::toMatchResponse)
                .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    public MatchResponse getByRoomCode(String roomCode) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        return toMatchResponse(match);
    }

    public void handlePlayerReady(Long matchId, Long userId) {
        MatchPlayer matchPlayer = matchPlayerRepository
                .findByMatchIdAndUserId(matchId, userId)
                .orElseThrow(() -> new RuntimeException("Player not in this match"));
        matchPlayer.setIsReady(true);
        matchPlayerRepository.save(matchPlayer);
    }

    public boolean areBothPlayersReady(Long matchId) {
        List<MatchPlayer> players = matchPlayerRepository.findByMatchId(matchId);
        return players.size() == 2 && players.stream().allMatch(MatchPlayer::getIsReady);
    }

    public void startMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        match.setStatus(MatchStatus.ACTIVE);
        match.setStartedAt(java.time.LocalDateTime.now());
        matchRepository.save(match);
    }

    public SubmissionResult processSubmission(Long matchId, SubmissionEvent event) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        List<TestCase> testCases = testCaseRepository.findByProblemId(
                match.getProblem().getId()
        );

        int totalTests = testCases.size();
        int testsPassed = 0;
        String finalStatus = "ACCEPTED";

        for (TestCase tc : testCases) {
            // Run the submitted code against this test case's input
            String actualOutput = codeExecutionService.execute(event.getCode(), tc.getInput());

            // Compare actual output to expected output
            if (actualOutput.equals(tc.getExpectedOutput().trim())) {
                testsPassed++;
            } else if (actualOutput.startsWith("COMPILE_ERROR")) {
                finalStatus = "COMPILE_ERROR";
                break; // no point running remaining tests if it doesn't compile
            } else if (actualOutput.equals("TIME_LIMIT_EXCEEDED")) {
                finalStatus = "TIME_LIMIT_EXCEEDED";
                break;
            } else {
                finalStatus = "WRONG_ANSWER";
            }
        }

        boolean allPassed = testsPassed == totalTests && totalTests > 0;
        if (allPassed) finalStatus = "ACCEPTED";

        // Save submission to database
        Submission submission = new Submission();
        submission.setMatch(match);
        submission.setUser(userRepository.findById(event.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found")));
        submission.setCode(event.getCode());
        submission.setLanguage(event.getLanguage());
        submission.setStatus(finalStatus);
        submission.setTestsPassed(testsPassed);
        submission.setTotalTests(totalTests);
        submission.setExecutionMs(0L);
        submissionRepository.save(submission);

        return new SubmissionResult(testsPassed, totalTests, allPassed, finalStatus, 0L);
    }

    public void finishMatch(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        User winner = userRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        match.setStatus(MatchStatus.FINISHED);
        match.setEndedAt(java.time.LocalDateTime.now());
        match.setWinner(winner);
        matchRepository.save(match);

        // Find the loser — the other player in this match
        Long loserId = matchPlayerRepository.findByMatchId(matchId)
                .stream()
                .map(mp -> mp.getUser().getId())
                .filter(id -> !id.equals(winnerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Loser not found"));

        // Update ELO for both players
        userService.updateElo(winnerId, loserId);
    }

    private MatchResponse toMatchResponse(Match match) {
        MatchResponse matchResponse = new MatchResponse();
        matchResponse.setId(match.getId());
        matchResponse.setRoomCode(match.getRoomCode());
        matchResponse.setProblemId(match.getProblem().getId());
        matchResponse.setProblemTitle(match.getProblem().getTitle());
        matchResponse.setStatus(match.getStatus().toString());
        return matchResponse;
    }

    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
