package com.codeduel.codeduel.repository;
import com.codeduel.codeduel.entity.MatchPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {
    List<MatchPlayer> findByMatchId(Long matchId);
    Optional<MatchPlayer> findByMatchIdAndUserId(Long matchId, Long userId);
}