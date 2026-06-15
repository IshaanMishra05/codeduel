package com.codeduel.repository;

import com.codeduel.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(String difficulty);
    List<Problem> findByLanguage(String language);
    List<Problem> findByDifficultyAndLanguage(String difficulty, String language);
}
