package com.codeduel.codeduel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomCode;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.WAITING;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;
}