package com.codeduel.codeduel.repository;
import com.codeduel.codeduel.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {}