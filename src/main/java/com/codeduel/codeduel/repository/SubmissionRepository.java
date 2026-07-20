package com.codeduel.codeduel.repository;
import com.codeduel.codeduel.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByMatchIdAndUserId(Long matchId, Long userId);
}