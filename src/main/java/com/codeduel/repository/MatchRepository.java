package com.codeduel.repository;

import com.codeduel.entity.Match;
import com.codeduel.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByRoomCode(String roomCode);
    List<Match> findByStatus(MatchStatus status);
    boolean existsByRoomCode(String roomCode);
}
