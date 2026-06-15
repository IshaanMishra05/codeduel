package com.codeduel.service;

import com.codeduel.dto.response.MatchResponse;
import com.codeduel.entity.*;
import com.codeduel.exception.BadRequestException;
import com.codeduel.exception.ResourceNotFoundException;
import com.codeduel.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock MatchRepository matchRepository;
    @Mock MatchPlayerRepository matchPlayerRepository;
    @Mock ProblemRepository problemRepository;

    @InjectMocks MatchService matchService;

    private User creator;
    private Problem problem;
    private Match savedMatch;

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .id(1L).username("alice").eloRating(1200)
                .wins(5).losses(3).totalMatches(8)
                .isActive(true).role(Role.PLAYER).build();

        problem = Problem.builder()
                .id(10L).title("Two Sum").difficulty("EASY").language("java")
                .build();

        savedMatch = Match.builder()
                .id(100L).roomCode("ABC123").problem(problem)
                .status(MatchStatus.WAITING).timeLimitSeconds(1800)
                .build();
    }

    @Test
    void createRoom_success_creatorAutoJoins() {
        when(problemRepository.findById(10L)).thenReturn(Optional.of(problem));
        when(matchRepository.save(any(Match.class))).thenReturn(savedMatch);
        when(matchRepository.existsByRoomCode(anyString())).thenReturn(false);
        when(matchPlayerRepository.save(any(MatchPlayer.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(matchPlayerRepository.findByMatchId(100L)).thenReturn(List.of(
                MatchPlayer.builder().match(savedMatch).user(creator).isReady(false).build()
        ));

        MatchResponse response = matchService.createRoom(10L, 1800, creator);

        assertThat(response.getRoomCode()).isEqualTo("ABC123");
        assertThat(response.getStatus()).isEqualTo(MatchStatus.WAITING);
        assertThat(response.getPlayers()).hasSize(1);
        verify(matchPlayerRepository).save(any(MatchPlayer.class));
    }

    @Test
    void createRoom_problemNotFound_throwsNotFound() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.createRoom(99L, 1800, creator))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Problem not found");
    }

    @Test
    void joinRoom_roomFull_throwsBadRequest() {
        when(matchRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(savedMatch));
        when(matchPlayerRepository.countByMatchId(100L)).thenReturn(2L);

        User bob = User.builder().id(2L).username("bob").eloRating(1100).build();

        assertThatThrownBy(() -> matchService.joinRoom("ABC123", bob))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Room is full");
    }

    @Test
    void joinRoom_matchNotWaiting_throwsBadRequest() {
        savedMatch.setStatus(MatchStatus.ACTIVE);
        when(matchRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(savedMatch));

        User bob = User.builder().id(2L).username("bob").build();

        assertThatThrownBy(() -> matchService.joinRoom("ABC123", bob))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer accepting");
    }

    @Test
    void joinRoom_alreadyIn_isIdempotent() {
        when(matchRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(savedMatch));
        when(matchPlayerRepository.countByMatchId(100L)).thenReturn(1L);
        when(matchPlayerRepository.existsByMatchIdAndUserId(100L, 1L)).thenReturn(true);
        when(matchPlayerRepository.findByMatchId(100L)).thenReturn(List.of(
                MatchPlayer.builder().match(savedMatch).user(creator).isReady(false).build()
        ));

        MatchResponse response = matchService.joinRoom("ABC123", creator);

        assertThat(response.getRoomCode()).isEqualTo("ABC123");
        verify(matchPlayerRepository, never()).save(any()); // should not add again
    }

    @Test
    void setReady_bothPlayersReady_startsMatch() {
        User bob = User.builder().id(2L).username("bob").eloRating(1100).build();

        MatchPlayer mpAlice = MatchPlayer.builder().match(savedMatch).user(creator).isReady(false).build();
        MatchPlayer mpBob   = MatchPlayer.builder().match(savedMatch).user(bob).isReady(true).build();

        when(matchRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(savedMatch));
        when(matchPlayerRepository.findByMatchIdAndUserId(100L, 1L)).thenReturn(Optional.of(mpAlice));
        when(matchPlayerRepository.save(mpAlice)).thenReturn(mpAlice);
        when(matchPlayerRepository.findByMatchId(100L)).thenReturn(List.of(mpAlice, mpBob));
        when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        MatchResponse response = matchService.setReady("ABC123", creator);

        assertThat(response.getStatus()).isEqualTo(MatchStatus.ACTIVE);
    }
}
