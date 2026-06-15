package com.codeduel.repository;

import com.codeduel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Leaderboard: top N by elo, only active players
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.totalMatches > 0 ORDER BY u.eloRating DESC")
    List<User> findLeaderboard(org.springframework.data.domain.Pageable pageable);
}
