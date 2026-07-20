package com.codeduel.codeduel.service;

import com.codeduel.codeduel.dto.request.MatchPlayerRequest;
import com.codeduel.codeduel.dto.response.MatchPlayerResponse;
import com.codeduel.codeduel.entity.Match;
import com.codeduel.codeduel.entity.MatchPlayer;
import com.codeduel.codeduel.entity.User;
import com.codeduel.codeduel.repository.MatchPlayerRepository;
import com.codeduel.codeduel.repository.MatchRepository;
import com.codeduel.codeduel.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchPlayerService {
    private final MatchPlayerRepository matchPlayerRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public MatchPlayerService(MatchPlayerRepository matchPlayerRepository, MatchRepository matchRepository, UserRepository userRepository) {
        this.matchPlayerRepository = matchPlayerRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    public MatchPlayerResponse join(MatchPlayerRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        MatchPlayer matchPlayer = new MatchPlayer();
        matchPlayer.setMatch(match);
        matchPlayer.setUser(user);
        matchPlayer.setIsReady(false);
        matchPlayerRepository.save(matchPlayer);
        return toMatchPlayerResponse(matchPlayer);
    }

    public List<MatchPlayerResponse> getByMatchId(Long matchId) {
        return matchPlayerRepository.findByMatchId(matchId)
                .stream()
                .map(this::toMatchPlayerResponse)
                .toList();
    }

    private MatchPlayerResponse toMatchPlayerResponse(MatchPlayer matchPlayer) {
        MatchPlayerResponse response = new MatchPlayerResponse();
        response.setId(matchPlayer.getId());
        response.setUsername(matchPlayer.getUser().getUsername());
        response.setIsReady(matchPlayer.getIsReady());
        return response;
    }
}
