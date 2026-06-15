package com.codeduel.repository;

import com.codeduel.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProblemId(Long problemId);
    List<TestCase> findByProblemIdAndIsHidden(Long problemId, Boolean isHidden);
    void deleteByProblemId(Long problemId);
}
