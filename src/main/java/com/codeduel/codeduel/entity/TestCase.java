package com.codeduel.codeduel.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "test_cases")
@Data
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    private Boolean isHidden = false;
}