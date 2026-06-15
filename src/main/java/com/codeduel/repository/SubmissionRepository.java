package com.codeduel.repository;

import com.codeduel.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByMatchIdAndUserId(Long matchId, Long userId);
    List<Submission> findByMatchId(Long matchId);
    Optional<Submission> findTopByMatchIdAndUserIdAndStatusOrderBySubmittedAtDesc(
            Long matchId, Long userId, String status);
}
